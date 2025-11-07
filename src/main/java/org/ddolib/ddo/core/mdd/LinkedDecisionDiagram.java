package org.ddolib.ddo.core.mdd;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.SubProblem;
import org.ddolib.ddo.core.cache.SimpleCache;
import org.ddolib.ddo.core.cache.Threshold;
import org.ddolib.ddo.core.compilation.CompilationConfig;
import org.ddolib.ddo.core.compilation.CompilationType;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.heuristics.variable.VariableHeuristic;
import org.ddolib.modeling.FastLowerBound;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Relaxation;
import org.ddolib.util.debug.DebugLevel;
import org.ddolib.util.debug.DebugUtil;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import static org.ddolib.util.MathUtil.saturatedAdd;
import static org.ddolib.util.MathUtil.saturatedDiff;

/**
 * This class implements a decision diagram as a linked structure (linked MDD).
 * <p>
 * Each node in the diagram is represented by a {@link Node} object, and edges
 * between nodes represent decisions made during the problem-solving process.
 * This class supports the compilation of exact, relaxed, and restricted decision
 * diagrams using various heuristics and dominance rules.
 * </p>
 *
 * <p>
 * The main responsibilities of this class include:
 * </p>
 * <ul>
 *     <li>Building the decision diagram layer by layer from an initial state.</li>
 *     <li>Managing exact and relaxed cutsets of nodes.</li>
 *     <li>Applying restrictions and relaxations to limit layer width.</li>
 *     <li>Computing local bounds and fast lower bounds.</li>
 *     <li>Exporting the decision diagram to DOT format for visualization.</li>
 *     <li>Interfacing with caches to optimize repeated computations.</li>
 * </ul>
 *
 * @param <T> the type of state used in the problem modeled by this decision diagram
 */
public final class LinkedDecisionDiagram<T> implements DecisionDiagram<T> {
    /**
     * The set of decisions that led to the root of this decision diagram.
     */
    private final Set<Decision> pathToRoot;

    /**
     * Nodes from the previous layer, mapped to their associated subproblems.
     */
    private final HashMap<Node, NodeSubProblem<T>> prevLayer = new HashMap<>();

    /**
     * Nodes to expand in the current layer.
     */
    private final List<NodeSubProblem<T>> currentLayer = new ArrayList<>();

    /**
     * Nodes in the next layer.
     */
    private final HashMap<T, Node> nextLayer = new HashMap<>();

    /**
     * Nodes in the last exact cutset or frontier cutset.
     */
    private final List<NodeSubProblem<T>> cutset = new ArrayList<>();

    /**
     * Indicates whether the MDD is exact (true) or contains relaxed/restricted nodes (false).
     */
    private boolean exact = true;

    /**
     * The best node in the terminal layer, if one exists.
     */
    private Node best = null;

    /**
     * Depth of the last exact layer.
     */
    private int depthLEL = -1;

    /**
     * String builder used for generating DOT representation of the MDD.
     */
    private final StringBuilder dotStr = new StringBuilder();

    /**
     * Maps edge hash codes to their DOT representation.
     */
    private final HashMap<Integer, String> edgesDotStr = new HashMap<>();

    /**
     * Debug level for additional checks and information during compilation.
     */
    private final DebugLevel debugLevel;

    /**
     * Configuration and parameters for compiling the decision diagram.
     */
    private final CompilationConfig<T> config;


    /**
     * Creates a new linked decision diagram.
     *
     * @param config The configuration object containing problem parameters, heuristics,
     *               relaxation operators, dominance checkers, and compilation settings.
     */
    public LinkedDecisionDiagram(CompilationConfig<T> config) {
        final SubProblem<T> residual = config.residual;
        final Node root = new Node(residual.getValue());
        this.pathToRoot = residual.getPath();
        this.nextLayer.put(residual.getState(), root);
        this.debugLevel = config.debugLevel;
        this.config = config;

    }

    /**
     * Compiles the decision diagram according to the configuration:
     * <ul>
     *     <li>Exact, relaxed, or restricted compilation type.</li>
     *     <li>Layer-wise variable ordering and heuristics.</li>
     *     <li>Application of relaxations or restrictions based on width limits.</li>
     *     <li>Construction of the DOT graph if export or debugging is enabled.</li>
     *     <li>Optional caching of thresholds for faster branch-and-bound computations.</li>
     * </ul>
     */
    @Override
    public void compile() {

        // initialize the compilation
        final int maxWidth = config.maxWidth;
        final SubProblem<T> residual = config.residual;

        dotStr.append("digraph ").append(config.compilationType.toString().toLowerCase()).append("{\n");

        // proceed to compilation
        final Problem<T> problem = config.problem;
        final Relaxation<T> relax = config.relaxation;
        final VariableHeuristic<T> var = config.variableHeuristic;
        final NodeSubProblemComparator<T> ranking = new NodeSubProblemComparator<>(config.stateRanking);
        final DominanceChecker<T> dominance = config.dominance;
        final Optional<SimpleCache<T>> cache = config.cache;
        double bestUb = config.bestUB;

        final Set<Integer> variables = varSet(config);

        int depthGlobalDD = residual.getPath().size();
        int depthCurrentDD = 0;
        int initialDepth = residual.getPath().size();


        Set<NodeSubProblem<T>> currentCutSet = new HashSet<>();

        // list of depth for the current relax compilation of the DD
        ArrayList<Integer> listDepths = cache.isPresent() ? new ArrayList<>() : null;
        // the list of NodeSubProblem of the corresponding depth
        ArrayList<ArrayList<NodeSubProblem<T>>> nodeSubProblemPerLayer = cache.isPresent() ? new ArrayList<>() : null;
        // the list of Threshold of the corresponding depth
        ArrayList<ArrayList<Threshold>> layersThresholds = cache.isPresent() ? new ArrayList<>() : null;
        // list of nodes pruned
        ArrayList<NodeSubProblem<T>> pruned = cache.isPresent() ? new ArrayList<>() : null;

        while (!variables.isEmpty()) {
            Integer nextVar = var.nextVariable(variables, nextLayer.keySet().iterator());
            // change the layer focus: what was previously the next layer is now
            // becoming the current layer
            this.prevLayer.clear();
            for (NodeSubProblem<T> n : this.currentLayer) {
                this.prevLayer.put(n.node, n);
            }
            this.currentLayer.clear();

            for (Entry<T, Node> e : this.nextLayer.entrySet()) {
                T state = e.getKey();
                Node node = e.getValue();
                if (node.type != NodeType.EXACT || !dominance.updateDominance(state,
                        depthGlobalDD, node.value)) {
                    double flb = config.flb.fastLowerBound(state, variables);
                    double rlb = saturatedAdd(node.value, flb);
                    node.flb = flb;
                    this.currentLayer.add(new NodeSubProblem<>(state, rlb, node));
                }
            }

            if (cache.isPresent()) {
                pruned.clear();
                if (depthGlobalDD > initialDepth) {
                    for (NodeSubProblem<T> n : this.currentLayer) {
                        if (cache.get().getLayer(depthGlobalDD).containsKey(n.state)
                                && cache.get().getThreshold(n.state, depthGlobalDD).isPresent()
                                && n.node.value >= cache.get().getThreshold(n.state, depthGlobalDD).get().getValue()) {
                            pruned.add(n);
                        }
                    }
                }
                this.currentLayer.removeAll(pruned);
            }
            this.nextLayer.clear();

            if (currentLayer.isEmpty()) {
                // there is no feasible solution to this subproblem, we can stop the compilation here
                return;
            }

            if (nextVar == null) {
                // Some variables simply can't be assigned
                return;
            } else {
                variables.remove(nextVar);
            }


            // If the current layer is too large, we need to shrink it down. 
            // Whether this shrinking down means that we want to perform a restriction
            // or a relaxation depends on the type of compilation which has been 
            // requested from this decision diagram  
            //
            // IMPORTANT NOTE:
            // The check is on depth 2 because the parent of the current layer is saved
            // if a LEL is to be remembered. In order to be sure
            // to make progress, we must be certain to develop AT LEAST one layer per 
            // mdd compiled otherwise the LEL is going to be the root of this MDD (and
            // we would be stuck in an infinite loop)
            if (depthCurrentDD >= 2 && currentLayer.size() > maxWidth) {
                switch (config.compilationType) {
                    case Restricted:
                        exact = false;
                        restrict(maxWidth, ranking);
                        break;
                    case Relaxed:
                        if (exact) {
                            exact = false;
                            if (config.cutSetType == CutSetType.LastExactLayer) {
                                cutset.addAll(prevLayer.values());
                                depthLEL = depthCurrentDD - 1;
                            }
                        }
                        relax(maxWidth, ranking, relax);
                        break;
                    case Exact:
                        /* nothing to do */
                        break;
                }
            }

            for (NodeSubProblem<T> n : currentLayer) {
                if (config.exportAsDot || debugLevel == DebugLevel.EXTENDED) {
                    dotStr.append(generateDotStr(n, false));
                }
                if (n.lb >= config.bestUB) {
                    continue;
                } else {
                    final Iterator<Integer> domain = problem.domain(n.state, nextVar);
                    while (domain.hasNext()) {
                        final int val = domain.next();
                        final Decision decision = new Decision(nextVar, val);

                        branchOn(n, decision, problem);
                    }
                }
                if (config.cutSetType == CutSetType.Frontier
                        && config.compilationType == CompilationType.Relaxed
                        && !exact && depthCurrentDD >= 2) {
                    if (variables.isEmpty() && n.node.type == NodeType.EXACT) {
                        currentCutSet.add(n);
                    }
                    if (n.node.type == NodeType.RELAXED) {
                        for (Edge e : n.node.edges) {
                            Node origin = e.origin;
                            if (origin.type == NodeType.EXACT) {
                                currentCutSet.add(prevLayer.get(origin));
                            }
                        }
                    }
                }
            }

            if (cache.isPresent() && config.compilationType == CompilationType.Relaxed) {
                listDepths.add(depthGlobalDD);
                nodeSubProblemPerLayer.add(new ArrayList<>());
                layersThresholds.add(new ArrayList<>());
                for (NodeSubProblem<T> n : this.currentLayer) {
                    nodeSubProblemPerLayer.get(depthCurrentDD).add(n);
                    layersThresholds.get(depthCurrentDD).add(new Threshold(Integer.MIN_VALUE, false));
                }
            }

            depthGlobalDD += 1;
            depthCurrentDD += 1;
        }
        if (config.compilationType == CompilationType.Relaxed && config.cutSetType == CutSetType.Frontier) {
            cutset.addAll(currentCutSet);
        }


        // finalize: find best
        for (Node n : nextLayer.values()) {
            if (best == null || n.value < best.value) {
                best = n;
            }
        }

        if (config.exportAsDot || debugLevel == DebugLevel.EXTENDED) {
            for (Entry<T, Node> entry : nextLayer.entrySet()) {
                T state = entry.getKey();
                Node node = entry.getValue();
                NodeSubProblem<T> subProblem = new NodeSubProblem<>(state, best.value, node);
                dotStr.append(generateDotStr(subProblem, true));
            }
        }


        if (cache.isPresent() && config.compilationType == CompilationType.Relaxed) {
            if (!cutset.isEmpty()) {
                computeLocalBounds();

                for (NodeSubProblem<T> n : cutset) {
                    if (n.node.isMarked) {
                        n.node.isInExactCutSet = true;
                    }
                }

                markNodesAboveExactCutSet(nodeSubProblemPerLayer, config.cutSetType);
                // update the cache to improve the next computation of the BB
                computeAndUpdateThreshold(cache.get(), listDepths, nodeSubProblemPerLayer,
                        layersThresholds, bestUb, config.cutSetType);
            }
        } else if (config.compilationType == CompilationType.Relaxed) {
            // Compute the local bounds of the nodes in the mdd *iff* this is a relaxed mdd
            computeLocalBounds();
        }


        if (debugLevel != DebugLevel.OFF && config.compilationType != CompilationType.Relaxed) {
            checkFlb(config.problem);
        }
    }

    /**
     * Returns whether the decision diagram is exact.
     *
     * @return {@code true} if the MDD is exact, {@code false} if relaxed/restricted nodes exist
     */
    @Override
    public boolean isExact() {
        return exact;
    }

    /**
     * Returns the value of the best solution found in this decision diagram, if any.
     *
     * @return an {@link Optional} containing the best value or empty if no solution exists
     */
    @Override
    public Optional<Double> bestValue() {
        if (best == null) {
            return Optional.empty();
        } else {
            return Optional.of(best.value);
        }
    }

    /**
     * Returns the set of decisions representing the best solution found in this MDD.
     *
     * @return an {@link Optional} containing the set of decisions in the best solution,
     * or empty if no solution exists
     */
    @Override
    public Optional<Set<Decision>> bestSolution() {
        if (best == null) {
            return Optional.empty();
        } else {
            Set<Decision> sol = new HashSet<>(pathToRoot);

            Edge eb = best.best;
            while (eb != null) {
                sol.add(eb.decision);
                updateBestEdgeColor(eb.hashCode(), "#6fb052");
                eb = eb.origin == null ? null : eb.origin.best;
            }
            return Optional.of(sol);
        }
    }

    /**
     * Returns an iterator over the nodes in the exact cutset, transformed into subproblems.
     *
     * @return iterator of subproblems in the exact cutset
     */
    @Override
    public Iterator<SubProblem<T>> exactCutset() {
        return new NodeSubProblemsAsSubProblemsIterator<>(cutset.iterator(), pathToRoot);
    }

    /**
     * Checks whether the best path found in a relaxed MDD consists entirely of exact nodes.
     *
     * @return {@code true} if the best path contains only exact nodes, {@code false} otherwise
     */
    @Override
    public boolean relaxedBestPathIsExact() {
        if (best == null) {
            return false;
        } else {
            Edge eb = best.best;
            while (eb != null) {
                if (eb.origin.type == NodeType.RELAXED)
                    return false;
                eb = eb.origin.best;
            }
            return true;
        }
    }

    /**
     * Exports the compiled decision diagram in DOT format.
     *
     * @return a string containing the DOT representation of the MDD
     */
    @Override
    public String exportAsDot() {
        for (String e : edgesDotStr.values()) {
            dotStr.append(e);
            dotStr.append("];\n");
        }
        dotStr.append("}");
        return dotStr.toString();
    }

    private record PathInfo(Decision decision, double flbOfOrigin, double lengthToEnd) {
    }


    // ------ METHODS FOR DEBUG ------


    /**
     * Given a node, returns the list of decisions taken from the root to reach this node.
     *
     * @param node A node of the mdd
     * @return The list of decisions took from the root to reach the input node.
     */
    private LinkedList<PathInfo> constructPathFromRoot(Node node, double lengthToEnd) {
        LinkedList<PathInfo> path = new LinkedList<>();
        Edge eb = node.best;
        double currentLength = lengthToEnd;
        while (eb != null) {
            currentLength += eb.weight;
            PathInfo info = new PathInfo(eb.decision, eb.origin.flb, currentLength);
            path.addFirst(info);
            if (debugLevel == DebugLevel.EXTENDED) updateBestEdgeColor(eb.hashCode(), "#ff0000");
            eb = eb.origin == null ? null : eb.origin.best;

        }
        return path;
    }


    /**
     * Given a list of decisions returns string describing the states from root.
     *
     * @param pathFromRoot A list of decision.
     * @param problem      The problem linked to this mdd.
     * @return A list of decisions of the generated states from root.
     */
    private LinkedList<String> constructStateDescriptionFromRoot(LinkedList<PathInfo> pathFromRoot,
                                                                 Problem<T> problem) {
        LinkedList<String> states = new LinkedList<>();
        T current = problem.initialState();
        int depth = 0;
        String msg = String.format("%-23s", depth + ".");
        for (PathInfo pathInfo : pathFromRoot) {
            msg += String.format("length to end: %6s", pathInfo.lengthToEnd);
            msg += String.format(" - flb: %6s", pathInfo.flbOfOrigin);
            if (pathInfo.flbOfOrigin - 1e-10 > pathInfo.lengthToEnd) msg += "!";
            msg += " - " + current.toString();
            msg += "\n" + pathInfo.decision;
            states.addLast(msg);
            depth++;
            msg = String.format("%-20s - ", depth + ". cost: " + problem.transitionCost(current,
                    pathInfo.decision));
            current = problem.transition(current, pathInfo.decision);


        }
        states.addLast(msg);
        states.addLast(current.toString());
        return states;
    }

    /**
     * Checks if the {@link FastLowerBound} is well-defined.
     * This method constructs longest path from terminal nodes and checks for each node the mdd
     * if the associated fast lower bound is larger than the identified path.
     *
     */
    private void checkFlb(Problem<T> problem) {
        DecimalFormat df = new DecimalFormat("#.##########");
        for (Node last : nextLayer.values()) {
            //For each node we save the longest path to last
            LinkedHashMap<Node, Double> parent = new LinkedHashMap<>();
            parent.put(last, 0.0);
            while (!parent.isEmpty()) {
                Entry<Node, Double> current = parent.pollFirstEntry();
                if (current.getKey().flb - 1e-10 > current.getValue()) {
                    LinkedList<PathInfo> pathFromRoot = constructPathFromRoot(current.getKey(),
                            current.getValue());
                    LinkedList<String> failedState = constructStateDescriptionFromRoot(pathFromRoot, problem);
                    String lastState = failedState.getLast();
                    lastState =
                            String.format(" - fub: %6s", current.getKey().flb) + "! - " + lastState;
                    lastState =
                            String.format("length to end: %6s", current.getValue()) + lastState;
                    failedState.removeLast();
                    lastState = failedState.getLast() + lastState;
                    failedState.removeLast();
                    failedState.addLast(lastState);
                    String statesStr = failedState.stream().map(Objects::toString).collect(Collectors.joining("\n\t"));
                    String failureMsg = String.format("Found node with upper bound (%s) lower than " +
                                    "its longest path (%s)\n", df.format(current.getKey().flb),
                            df.format(current.getValue()));
                    failureMsg += String.format("Path from root: \n\t%s\n\n", statesStr);
                    failureMsg += String.format("Failing state: %s\n", failedState.getLast());
                    if (debugLevel == DebugLevel.EXTENDED) {
                        String dot = exportAsDot();
                        try (BufferedWriter bw =
                                     new BufferedWriter(new FileWriter(Paths.get("output",
                                             "failed.dot").toString()))) {
                            bw.write(dot);
                            failureMsg += "MDD saved in output/failed.dot\n";
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    throw new RuntimeException(failureMsg);
                }

                for (Edge edge : current.getKey().edges) {
                    double longestFromParent = parent.getOrDefault(edge.origin, Double.NEGATIVE_INFINITY);
                    parent.put(edge.origin, Double.max(longestFromParent, edge.weight + current.getValue()));
                }
            }
        }
    }


    /**
     * Checks whether the relaxation is coherent. This method compiles mdd starting from a
     * relaxed node and each merged nodes. Then, it compares the best values of each solution.
     * The path passing through the relaxed node must be a lower bound.
     *
     */
    private void checkRelaxation(List<NodeSubProblem<T>> nodesToMerge,
                                 NodeSubProblem<T> relaxedNode) {
        DecimalFormat df = new DecimalFormat("#.##########");

        Set<Decision> pathFromRelaxedToRoot = constructPathToRoot(relaxedNode.node);
        SubProblem<T> relaxedSub = new SubProblem<>(relaxedNode.state, relaxedNode.node.value,
                relaxedNode.lb, pathFromRelaxedToRoot);

        int relaxationDepth = relaxedSub.getDepth();
        LinkedDecisionDiagram<T> relaxedMdd = compileSubMdd(relaxedSub);
        Optional<Double> bestWithRelaxed = relaxedMdd.bestValue();
        Optional<Set<Decision>> bestRelaxedSol = relaxedMdd.bestSolution();
        Optional<Double> bestTransitionToRelaxed = Optional.of(relaxedNode.node.best.weight);

        for (NodeSubProblem<T> node : nodesToMerge) {
            SubProblem<T> sub = node.toSubProblem(constructPathToRoot(node.node));
            LinkedDecisionDiagram<T> mdd = compileSubMdd(sub);
            Optional<Double> bestWithNode = mdd.bestValue();
            Optional<Set<Decision>> bestSol = mdd.bestSolution();
            String failureMsg = "";
            if (bestWithRelaxed.isPresent()
                    && bestWithNode.isPresent()
                    && bestWithRelaxed.get() - 1e-10 > bestWithNode.get()) {

                failureMsg = String.format("Found relaxed node that lead to worst solution" +
                                " (%s) than one of the merged nodes (%s).\n",
                        df.format(bestWithRelaxed.get()), df.format(bestWithNode.get()));

                failureMsg += "Depth: " + relaxationDepth + "\n";
                failureMsg += "Relaxed state: " + relaxedNode.state + "\n";
                failureMsg += "Merged states state:\n\t" + nodesToMerge
                        .stream().map(n -> n.state.toString()).collect(Collectors.joining("\n\t"));
                failureMsg += String.format("\n\nPath by relaxed node : %s - value: %s\n",
                        relaxedNode.state, df.format(bestWithRelaxed.get()));
                failureMsg += describePath(bestRelaxedSol.get(), Optional.of(relaxationDepth),
                        Optional.of(relaxedSub.getState()), bestTransitionToRelaxed);

                failureMsg += String.format("\n\nPath by exact node : %s - value: %s\n",
                        node.state, df.format(bestWithNode.get()));
                failureMsg += describePath(bestSol.get(), Optional.empty(), Optional.empty(),
                        Optional.empty());

            } else if (bestWithRelaxed.isEmpty() && bestWithNode.isPresent()) {
                failureMsg = "Found relaxed node that lead to no solution but not the " +
                        "merged ones.\n";
                failureMsg += "Relaxed state: " + relaxedNode.state;
                failureMsg += "\nMerged states state:\n\t" + nodesToMerge
                        .stream().map(n -> n.state.toString()).collect(Collectors.joining("\n\t"));
            }

            if (!failureMsg.isEmpty()) {
                failureMsg += "\n";
                if (debugLevel == DebugLevel.EXTENDED) {
                    dotStr.append(generateDotStr(relaxedNode, false));
                    String dot = exportAsDot();
                    try (BufferedWriter bw =
                                 new BufferedWriter(new FileWriter(Paths.get("output",
                                         "failed.dot").toString()))) {
                        bw.write(dot);
                        failureMsg += "MDD saved in output/failed.dot\n";
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                throw new RuntimeException(failureMsg);
            }
        }
    }

    /**
     * Given a node sub-problem compiles the associated mdd and returns it.
     */
    private LinkedDecisionDiagram<T> compileSubMdd(SubProblem<T> sub) {
        CompilationConfig<T> compilation = config.copy();
        compilation.residual = sub;
        compilation.exportAsDot = false;
        compilation.debugLevel = DebugLevel.OFF;
        compilation.compilationType = CompilationType.Exact;

        LinkedDecisionDiagram<T> mdd = new LinkedDecisionDiagram<>(compilation);
        mdd.compile();
        return mdd;
    }


    /**
     * Returns the set of decision leading the root to the input node.
     *
     * @param node The target node.
     * @return The set of decision leading the root to the input node.
     */
    private Set<Decision> constructPathToRoot(Node node) {
        Edge eb = node.best;
        Set<Decision> path = new HashSet<>();
        while (eb != null) {
            Decision decision = eb.decision;
            path.add(decision);
            eb = eb.origin.best;
        }
        return path;
    }

    /**
     * Given a set of decision going from the root to a terminal node, returns a description of
     * the path.
     *
     */
    private String describePath(Set<Decision> pathFromRoot, Optional<Integer> relaxationDepth,
                                Optional<T> relaxedState, Optional<Double> relaxedCost) {
        List<Decision> path = pathFromRoot.stream().sorted(Comparator.comparingInt(Decision::var)).toList();
        T current = config.problem.initialState();
        int depth = 0;
        StringBuilder msg = new StringBuilder(String.format("\t\t%-23s", depth + "."));
        for (Decision decision : path) {
            msg.append(current).append("\n\t");
            msg.append(decision);
            msg.append("\n");
            depth++;

            double cost;
            if (relaxationDepth.isPresent() && depth == relaxationDepth.get()) {
                cost = relaxedCost.get();
                current = relaxedState.get();
            } else {
                cost = config.problem.transitionCost(current, decision);
                current = config.problem.transition(current, decision);
            }

            msg.append(String.format("\t\t%-20s - ", depth + ". cost: " + cost));

        }
        msg.append(current);
        return msg.toString();
    }

    // UTILITY METHODS -----------------------------------------------
    private Set<Integer> varSet(final CompilationConfig<T> input) {
        final HashSet<Integer> set = new HashSet<>();
        for (int i = 0; i < config.problem.nbVars(); i++) {
            set.add(i);
        }

        for (Decision d : input.residual.getPath()) {
            set.remove(d.var());
        }
        return set;
    }

    /**
     * Performs a restriction of the current layer.
     *
     * @param maxWidth the maximum tolerated layer width
     * @param ranking  a ranking that orders the nodes from the most promising (greatest)
     *                 to the least promising (lowest)
     */
    private void restrict(final int maxWidth, final NodeSubProblemComparator<T> ranking) {
        this.currentLayer.sort(ranking);
        this.currentLayer.subList(maxWidth, this.currentLayer.size()).clear(); // truncate
    }

    /**
     * Performs a restriction of the current layer.
     *
     * @param maxWidth the maximum tolerated layer width
     * @param ranking  a ranking that orders the nodes from the most promising (greatest)
     *                 to the least promising (lowest)
     * @param relax    the relaxation operators which we will use to merge nodes
     */
    private void relax(final int maxWidth, final NodeSubProblemComparator<T> ranking,
                       final Relaxation<T> relax) {
        this.currentLayer.sort(ranking);

        final List<NodeSubProblem<T>> keep = this.currentLayer.subList(0, maxWidth - 1);
        final List<NodeSubProblem<T>> merge = this.currentLayer.subList(maxWidth - 1, currentLayer.size());
        final T merged = relax.mergeStates(new NodeSubProblemsAsStateIterator<>(merge.iterator()));

        // is there another state in the kept partition having the same state as the merged state ?
        NodeSubProblem<T> mergedNode = null;
        boolean fresh = true;
        for (NodeSubProblem<T> n : keep) {
            if (n.state.equals(merged)) {
                mergedNode = n;
                fresh = false;
                break;
            }
        }
        // when the merged node is new, set its type to relaxed
        if (mergedNode == null) {
            Node newNode = new Node(Double.POSITIVE_INFINITY);
            newNode.type = NodeType.RELAXED;
            mergedNode = new NodeSubProblem<>(merged, Double.POSITIVE_INFINITY, newNode);
        }

        // redirect and relax all arcs entering the merged node
        for (NodeSubProblem<T> drop : merge) {
            mergedNode.lb = Math.min(mergedNode.lb, drop.lb);

            for (Edge e : drop.node.edges) {
                double rcost = relax.relaxEdge(prevLayer.get(e.origin).state, drop.state, merged, e.decision, e.weight);

                double value = saturatedAdd(e.origin.value, rcost);
                e.weight = rcost;
                // if there exists an entring arc with relaxed origin, set the merged node to relaxed
                if (e.origin.type == NodeType.RELAXED) {
                    mergedNode.node.type = NodeType.RELAXED;
                }
                mergedNode.node.edges.add(e);
                if (value < mergedNode.node.value) {
                    mergedNode.node.value = value;
                    mergedNode.node.best = e;
                }
            }
        }


        if (debugLevel != DebugLevel.OFF) {
            checkRelaxation(merge, mergedNode);
        }


        // delete the nodes that have been merged
        merge.clear();
        // append the newly merged node if needed
        if (fresh) {
            currentLayer.add(mergedNode);
        }
    }

    /**
     * This method performs the branching from the subproblem rooted in "node", making the given decision
     * and behaving as per the problem definition.
     *
     * @param node     the origin of the transition
     * @param decision the decision being made
     * @param problem  the problem that defines the transition and transition cost functions
     */
    private void branchOn(final NodeSubProblem<T> node,
                          final Decision decision,
                          final Problem<T> problem) {
        if (debugLevel != DebugLevel.OFF)
            DebugUtil.checkHashCodeAndEquality(node.state, decision, problem::transition);

        T state = problem.transition(node.state, decision);
        double cost = problem.transitionCost(node.state, decision);
        double value = saturatedAdd(node.node.value, cost);

        // when the origin is relaxed, the destination must be relaxed
        Node n = nextLayer.get(state);
        if (n == null) {
            n = new Node(value);
            if (node.node.type == NodeType.RELAXED) {
                n.type = NodeType.RELAXED;
            }
            nextLayer.put(state, n);
        } else {
            if (node.node.type == NodeType.RELAXED) {
                n.type = NodeType.RELAXED;
            }
        }

        Edge edge = new Edge(node.node, decision, cost);
        n.edges.add(edge);
        if (value <= n.value) {
            n.best = edge;
            n.value = value;
        }
    }


    /**
     * Performs a bottom up traversal of the mdd to compute the local bounds
     */
    private void computeLocalBounds() {
        HashSet<Node> current = new HashSet<>();
        HashSet<Node> parent = new HashSet<>(nextLayer.values());

        for (Node n : parent) {
            n.suffix = 0.0;
            n.isMarked = true;
        }

        while (!parent.isEmpty()) {
            HashSet<Node> tmp = current;
            current = parent;
            parent = tmp;
            parent.clear();

            for (Node n : current) {
                if (n.isMarked) {
                    for (Edge e : n.edges) {
                        // Note: we might want to do something and stop as soon as the lel has been reached
                        Node origin = e.origin;
                        parent.add(origin);

                        if (origin.suffix == null) {
                            origin.suffix = saturatedAdd(n.suffix, e.weight);
                        } else {
                            origin.suffix = Math.min(origin.suffix, saturatedAdd(n.suffix, e.weight));
                        }
                        origin.isMarked = true;
                    }
                }
            }
        }
    }

    /**
     * Given a node, returns the .dot formatted string containing the node and the edges leading to this node.
     *
     * @param node      The node to add to the .dot string
     * @param lastLayer Whether the given node is in the last layer. Used to give it a dedicated format.
     * @return A .dot formatted string containing the node and the edges leading to this node.
     */
    private StringBuilder generateDotStr(NodeSubProblem<T> node, boolean lastLayer) {
        DecimalFormat df = new DecimalFormat("#.##########");

        if (lastLayer) {
            node.lb = config.flb.fastLowerBound(node.state, new HashSet<>());
        }

        String nodeStr = String.format(
                "\"%s\nflb: %s - value: %s\"",
                node.state,
                df.format(node.lb),
                df.format(node.node.value)
        );

        StringBuilder sb = new StringBuilder();
        sb.append(node.node.hashCode());
        sb.append(" [label=").append(nodeStr);
        if (node.node.type == NodeType.RELAXED) {
            sb.append(", shape=box, tooltip=\"Relaxed node\"");
        } else {
            sb.append(", style=rounded, shape=rectangle, tooltip=\"Exact node\"");
        }
        if (lastLayer) {
            sb.append(", style=\"filled, rounded\", shape=rectangle, color=black, fontcolor=white");
            sb.append(", tooltip=\"Terminal node\"");
        }
        sb.append("];\n");

        for (Edge e : node.node.edges) {
            String edgeStr = e.origin.hashCode() + " -> " + node.node.hashCode() +
                    " [label=" + df.format(e.weight) +
                    ", tooltip=\"" + e.decision.toString() + "\"";
            edgesDotStr.put(e.hashCode(), edgeStr);
        }
        return sb;
    }

    /**
     * Given the hashcode of an edge, updates its color. Used when the best solution is constructed.
     *
     * @param edgeHash The hashcode of the edge to color.
     * @param color    HTML string for the color of the edge
     */
    private void updateBestEdgeColor(int edgeHash, String color) {
        String edgeStr = edgesDotStr.get(edgeHash);
        if (edgeStr != null) {
            edgeStr += ", color=\"" + color + "\", fontcolor=\"" + color + "\"";
            edgesDotStr.replace(edgeHash, edgeStr);
        }
    }

    private void markNodesAboveExactCutSet(ArrayList<ArrayList<NodeSubProblem<T>>> nodePerLayer, CutSetType cutSetType) {
        HashSet<Node> current = new HashSet<>();
        HashSet<Node> parent = new HashSet<>();

        if (cutSetType == CutSetType.LastExactLayer) {
            for (NodeSubProblem<T> n : nodePerLayer.get(depthLEL)) {
                parent.add(n.node);
            }
        } else {
            parent.addAll(nextLayer.values());
        }

        while (!parent.isEmpty()) {
            HashSet<Node> tmp = current;
            current = parent;
            parent = tmp;
            parent.clear();

            for (Node n : current) {
                for (Edge e : n.edges) {
                    // Note: we might want to do something and stop as soon as the lel has been reached
                    Node origin = e.origin;
                    parent.add(origin);
                    if ((n.isInExactCutSet || n.isAboveExactCutSet)
                            && origin.type == NodeType.EXACT
                            && !origin.isInExactCutSet) {
                        origin.isAboveExactCutSet = true;
                    }
                }
            }
        }
    }

    /**
     * Performs the bottom up traversal of the mdd to compute and update the cache
     */
    private void computeAndUpdateThreshold(SimpleCache<T> simpleCache,
                                           ArrayList<Integer> listDepth,
                                           ArrayList<ArrayList<NodeSubProblem<T>>> nodePerLayer,
                                           ArrayList<ArrayList<Threshold>> currentCache,
                                           double ub,
                                           CutSetType cutSetType) {
        for (int j = listDepth.size() - 1; j >= 0; j--) {
            int depth = listDepth.get(j);
            for (int i = 0; i < nodePerLayer.get(j).size(); i++) {
                NodeSubProblem<T> sub = nodePerLayer.get(j).get(i);
                if (simpleCache.getLayer(depth).containsKey(sub.state)
                        && simpleCache.getLayer(depth).get(sub.state).isPresent()
                        && sub.node.value >= simpleCache.getLayer(depth).get(sub.state).get().getValue()) {
                    double value = simpleCache.getLayer(depth).get(sub.state).get().getValue();
                    currentCache.get(j).get(i).setValue(value);
                } else {
                    if (sub.lb >= ub) {
                        double rlb = saturatedDiff(sub.lb, sub.node.value);
                        double value = saturatedDiff(ub, rlb);
                        currentCache.get(j).get(i).setValue(value);
                    } else if (sub.node.isInExactCutSet) {
                        if (sub.node.suffix != null && saturatedAdd(sub.node.value, sub.node.suffix) >= ub) {
                            double value = Math.min(currentCache.get(j).get(i).getValue(), saturatedDiff(ub, sub.node.suffix));
                            currentCache.get(j).get(i).setValue(value);
                        } else {
                            currentCache.get(j).get(i).setValue(sub.node.value);
                        }
                    }
                    if (sub.node.type == NodeType.EXACT) {
                        if (sub.node.isAboveExactCutSet && !sub.node.isInExactCutSet) {
                            currentCache.get(j).get(i).setExplored(true);
                        }
                        if (cutSetType == CutSetType.LastExactLayer
                                && sub.node.value > currentCache.get(j).get(i).getValue()
                                && sub.node.isInExactCutSet)
                            currentCache.get(j).get(i).setExplored(true);
                        if (currentCache.get(j).get(i).isExplored()) {
                            simpleCache.getLayer(depth).update(sub.state, currentCache.get(j).get(i));
                        }
                    }
                }
                for (Edge e : sub.node.edges) {
                    Node origin = e.origin;
                    int index = -1;
                    for (int k = 0; k < nodePerLayer.get(j - 1).size(); k++) {
                        if (nodePerLayer.get(j - 1).get(k).node.equals(origin)) {
                            index = k;
                            break;
                        }
                    }
                    double value = Math.max(currentCache.get(j - 1).get(index).getValue(), saturatedDiff(currentCache.get(j).get(i).getValue(), e.weight));
                    currentCache.get(j - 1).get(index).setValue(value);
                }
            }
        }
    }


}

