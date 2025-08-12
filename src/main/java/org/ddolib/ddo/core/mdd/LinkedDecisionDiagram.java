package org.ddolib.ddo.core.mdd;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.SubProblem;
import org.ddolib.ddo.core.compilation.CompilationInput;
import org.ddolib.ddo.core.compilation.CompilationType;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.heuristics.variable.VariableHeuristic;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Relaxation;
import org.ddolib.modeling.StateRanking;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.*;
import java.util.Map.Entry;

/**
 * This class implements the decision diagram as a linked structure.
 *
 * @param <T> the type of state
 * @param <K> the type of key
 */
public final class LinkedDecisionDiagram<T, K> implements DecisionDiagram<T, K> {
    /**
     * The list of decisions that have led to the root of this DD
     */
    private Set<Decision> pathToRoot = Collections.emptySet();
    /**
     * All the nodes from the previous layer
     */
    private HashMap<Node, NodeSubProblem<T>> prevLayer = new HashMap<>();
    /**
     * All the (subproblems) nodes from the previous layer -- That is, all nodes that will be expanded
     */
    private List<NodeSubProblem<T>> currentLayer = new ArrayList<>();
    /**
     * All the nodes from the next layer
     */
    private HashMap<T, Node> nextLayer = new HashMap<T, Node>();
    /**
     * All the nodes from the last exact layer cutset or the frontier cutset
     */
    private List<NodeSubProblem<T>> cutset = new ArrayList<>();
    /**
     * A flag to keep track of the fact that LEL might be empty albeit not set
     */

    /**
     * A flag to keep track of the fact the MDD was relaxed (some merged occurred) or restricted  (some states were dropped)
     */
    private boolean exact = true;

    /**
     * The best node in the terminal layer (if it exists at all)
     */
    private Node best = null;

    /**
     * Used to build the .dot file displaying the compiled mdd.
     */
    private StringBuilder dotStr = new StringBuilder();
    /**
     * Given the hashcode of an edge, save its .dot representation
     */
    private HashMap<Integer, String> edgesDotStr = new HashMap<>();

    // --- UTILITY CLASSES -----------------------------------------------

    /**
     * This is an atomic node from the decision diagram. Per-se, it does not
     * hold much interpretable information.
     */
    private static final class Node {
        /**
         * The length of the longest path to this node
         */
        private double value;
        /**
         * The length of the longest suffix of this node (bottom part of a local bound)
         */
        private Double suffix;
        /**
         * The edge terminating the longest path to this node
         */
        private Edge best;
        /**
         * The list of edges leading to this node
         */
        private List<Edge> edges;

        /**
         * The type of this node (exact, relaxed, etc...)
         */
        private NodeType type;

        /**
         * The falg to indicate if a node is marked
         */
        private boolean isMarked;

        private double ub = Double.POSITIVE_INFINITY;

        /**
         * Creates a new node
         */
        public Node(final double value) {
            this.value = value;
            this.suffix = null;
            this.best = null;
            this.edges = new ArrayList<>();
            this.type = NodeType.EXACT;
            this.isMarked = false;
        }

        /**
         * set the type of the node when different to exact type
         *
         * @param nodeType
         */
        public void setNodeType(final NodeType nodeType) {
            this.type = nodeType;
        }

        public void setUb(final double ub) {
            this.ub = ub;
        }

        /**
         * get the type of the node
         *
         * @return NodeType
         */
        public NodeType getNodeType() {
            return this.type;
        }

        @Override
        public String toString() {
            return String.format("Node: value:%.0f - suffix: %s - best edge: %s - parent edges: %s",
                    value, suffix, best, edges);
        }

        // Deterministic hash
        private static int nextHash = 0;
        private final int hash = nextHash++;

        @Override
        public int hashCode() {
            return hash;
        }
    }

    /**
     * Flag to identify the type of node: exact node, relaxed node, marked node, etc ...
     */
    public enum NodeType {
        EXACT, RELAXED
    }

    /**
     * This is an edge that connects two nodes from the decision diagram
     */
    private static final class Edge {
        /**
         * The source node of this arc
         */
        private Node origin;
        /**
         * The decision that was made when traversing this arc
         */
        private Decision decision;
        /**
         * The weight of the arc
         */
        private double weight;

        /**
         * Creates a new edge between pairs of nodes
         *
         * @param src the source node
         * @param d   the decision that was made when traversing this edge
         * @param w   the weight of the edge
         */
        public Edge(final Node src, final Decision d, final double w) {
            this.origin = src;
            this.decision = d;
            this.weight = w;
        }

        @Override
        public String toString() {
            return String.format("Origin:%s\n\t Decision:%s\n\t Weight:%s ", origin, decision, weight);
        }
    }

    /**
     * This class encapsulates the association of a node with its state and
     * associated rough upper bound.
     * <p>
     * This class essentially serves two purposes:
     * <p>
     * - associate a node with a state during the compilation (and allow to
     * eagerly forget about the given state, which allows to save substantial
     * amounts of RAM while compiling the DD).
     * <p>
     * - turn an MDD node from the exact cutset into a subproblem which is used
     * by the API.
     */
    private static final class NodeSubProblem<T> {
        /**
         * The state associated to this node
         */
        private final T state;
        /**
         * The actual node from the graph of decision diagrams
         */
        private final Node node;
        /**
         * The upper bound associated with this node (if state were the root)
         */
        private double ub;

        /**
         * Creates a new instance
         */
        public NodeSubProblem(final T state, final double ub, final Node node) {
            this.state = state;
            this.ub = ub;
            this.node = node;
        }

        /**
         * @return Turns this association into an actual subproblem
         */
        public SubProblem<T> toSubProblem(final Set<Decision> pathToRoot) {
            HashSet<Decision> path = new HashSet<>();
            path.addAll(pathToRoot);

            Edge e = node.best;
            while (e != null) {
                path.add(e.decision);
                e = e.origin == null ? null : e.origin.best;
            }

            double locb = Double.NEGATIVE_INFINITY;
            if (node.suffix != null) {
                locb = saturatedAdd(node.value, node.suffix);
            }
            ub = Math.min(ub, locb);

            return new SubProblem<>(state, node.value, ub, path);
        }

        @Override
        public String toString() {
            DecimalFormat df = new DecimalFormat("#.##########");
            return String.format("%s - ub: %s - value: %s", state, df.format(ub), df.format(node.value));
        }
    }

    @Override
    public void compile(CompilationInput<T, K> input) {
        // make sure we don't have any stale data left
        this.clear();

        // initialize the compilation
        final int maxWidth = input.maxWidth();
        final SubProblem<T> residual = input.residual();
        final Node root = new Node(residual.getValue());
        this.pathToRoot = residual.getPath();
        this.nextLayer.put(residual.getState(), root);

        dotStr.append("digraph ").append(input.compilationType().toString().toLowerCase()).append("{\n");

        // proceed to compilation
        final Problem<T> problem = input.problem();
        final Relaxation<T> relax = input.relaxation();
        final VariableHeuristic<T> var = input.variableHeuristic();
        final NodeSubroblemComparator<T> ranking = new NodeSubroblemComparator<>(input.stateRanking());
        final DominanceChecker<T, K> dominance = input.dominance();

        final Set<Integer> variables = varSet(input);

        int depthGlobalDD = residual.getPath().size();
        int depthCurrentDD = 0;


        Set<NodeSubProblem<T>> currentCutSet = new HashSet<>();

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
                if (node.getNodeType() == NodeType.EXACT && dominance.updateDominance(state, depthGlobalDD, node.value)) {
                    continue;
                } else {
                    double rub = saturatedAdd(node.value, input.fub().fastUpperBound(state, variables));
                    node.setUb(rub);
                    this.currentLayer.add(new NodeSubProblem<>(state, rub, node));
                }
            }
            this.nextLayer.clear();

            if (currentLayer.isEmpty()) {
                // there is no feasible solution to this subproblem, we can stop the compilation here
                return;
            }

            if (nextVar == null) {
                // Some variables simply can't be assigned
                clear();
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
                switch (input.compilationType()) {
                    case Restricted:
                        exact = false;
                        restrict(maxWidth, ranking);
                        break;
                    case Relaxed:
                        if (exact) {
                            exact = false;
                            if (input.cutSetType() == CutSetType.LastExactLayer) {
                                cutset.addAll(prevLayer.values());
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
                if (input.exportAsDot() || input.debugLevel() >= 2) {
                    dotStr.append(generateDotStr(n, false));
                }
                if (n.ub <= input.bestLB()) {
                    continue;
                } else {
                    final Iterator<Integer> domain = problem.domain(n.state, nextVar);
                    while (domain.hasNext()) {
                        final int val = domain.next();
                        final Decision decision = new Decision(nextVar, val);

                        branchOn(n, decision, problem);
                    }
                }
                if (input.cutSetType() == CutSetType.Frontier && input.compilationType() == CompilationType.Relaxed && !exact && depthCurrentDD >= 2) {
                    if (variables.isEmpty() && n.node.getNodeType() == NodeType.EXACT) {
                        currentCutSet.add(n);
                    }
                    if (n.node.getNodeType() == NodeType.RELAXED) {
                        for (Edge e : n.node.edges) {
                            Node origin = e.origin;
                            if (origin.getNodeType() == NodeType.EXACT) {
                                currentCutSet.add(prevLayer.get(origin));
                            }
                        }
                    }
                }
            }

            depthGlobalDD += 1;
            depthCurrentDD += 1;
        }
        if (input.compilationType() == CompilationType.Relaxed && input.cutSetType() == CutSetType.Frontier) {
            cutset.addAll(currentCutSet);
        }


        // finalize: find best
        for (Node n : nextLayer.values()) {
            if (best == null || n.value > best.value) {
                best = n;
            }
        }

        if (input.exportAsDot() || input.debugLevel() >= 2) {
            for (Entry<T, Node> entry : nextLayer.entrySet()) {
                T state = entry.getKey();
                Node node = entry.getValue();
                NodeSubProblem<T> subProblem = new NodeSubProblem<>(state, best.value, node);
                dotStr.append(generateDotStr(subProblem, true));
            }
        }


        // Compute the local bounds of the nodes in the mdd *iff* this is a relaxed mdd
        if (input.compilationType() == CompilationType.Relaxed) {
            computeLocalBounds();
        }
        if (input.debugLevel() >= 1 && input.compilationType() != CompilationType.Relaxed) {
            checkFub(input.debugLevel());
        }
    }

    @Override
    public boolean isExact() {
        return exact;
        //return !lelWasSet;
    }

    @Override
    public Optional<Double> bestValue() {
        if (best == null) {
            return Optional.empty();
        } else {
            return Optional.of(best.value);
        }
    }

    @Override
    public Optional<Set<Decision>> bestSolution() {
        if (best == null) {
            return Optional.empty();
        } else {
            Set<Decision> sol = new HashSet<>(pathToRoot);

            Edge eb = best.best;
            while (eb != null) {
                sol.add(eb.decision);
                updateBestEdgeColor(eb.hashCode());
                eb = eb.origin == null ? null : eb.origin.best;
            }
            return Optional.of(sol);
        }
    }

    @Override
    public Iterator<SubProblem<T>> exactCutset() {
        return new NodeSubProblemsAsSubProblemsIterator<>(cutset.iterator(), pathToRoot);
    }

    @Override
    public boolean relaxedBestPathIsExact() {
        if (best == null) {
            return false;
        } else {
            Edge eb = best.best;
            while (eb != null) {
                if (eb.origin.getNodeType() == NodeType.RELAXED)
                    return false;
                eb = eb.origin.best;
            }
            return true;
        }
    }

    @Override
    public String exportAsDot() {
        for (String e : edgesDotStr.values()) {
            dotStr.append(e);
            dotStr.append("];\n");
        }
        dotStr.append("}");
        return dotStr.toString();
    }

    private void checkFub(int debugLevel) {
        DecimalFormat df = new DecimalFormat("#.##########");
        for (Node last : nextLayer.values()) {
            double lastValue = last.value;
            //For each node we save the longest path to last
            LinkedHashMap<Node, Double> parent = new LinkedHashMap<>();
            parent.put(last, 0.0);
            while (!parent.isEmpty()) {
                Entry<Node, Double> current = parent.pollFirstEntry();
                double longest = current.getKey().value + current.getValue();
                if (current.getKey().ub < longest) {
                    if (debugLevel >= 2) {
                        String dot = exportAsDot();
                        try (BufferedWriter bw =
                                     new BufferedWriter(new FileWriter(Paths.get("output",
                                             "failed.dot").toString()))) {
                            bw.write(dot);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    String failureMsg = String.format("Found node with upper bound (%s) lower than " +
                            "its longest path (%s)", df.format(current.getKey().ub), df.format(lastValue));
                    throw new RuntimeException(failureMsg);
                }

                for (Edge edge : current.getKey().edges) {
                    double longestFromParent = parent.getOrDefault(edge.origin, Double.NEGATIVE_INFINITY);
                    parent.put(edge.origin, Double.max(longestFromParent, edge.weight + current.getValue()));
                }
            }
        }
    }

    // --- UTILITY METHODS -----------------------------------------------
    private Set<Integer> varSet(final CompilationInput<T, K> input) {
        final HashSet<Integer> set = new HashSet<>();
        for (int i = 0; i < input.problem().nbVars(); i++) {
            set.add(i);
        }

        for (Decision d : input.residual().getPath()) {
            set.remove(d.var());
        }
        return set;
    }

    /**
     * Reset the state of this MDD. This way it can easily be reused
     */
    private void clear() {
        pathToRoot = Collections.emptySet();
        prevLayer.clear();
        currentLayer.clear();
        nextLayer.clear();
        cutset.clear();
        exact = true;
        best = null;
        dotStr = new StringBuilder();
        edgesDotStr = new HashMap<>();
    }

    /**
     * Performs a restriction of the current layer.
     *
     * @param maxWidth the maximum tolerated layer width
     * @param ranking  a ranking that orders the nodes from the most promising (greatest)
     *                 to the least promising (lowest)
     */
    private void restrict(final int maxWidth, final NodeSubroblemComparator<T> ranking) {
        this.currentLayer.sort(ranking.reversed());
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
    private void relax(final int maxWidth, final NodeSubroblemComparator<T> ranking, final Relaxation<T> relax) {
        this.currentLayer.sort(ranking.reversed());

        final List<NodeSubProblem<T>> keep = this.currentLayer.subList(0, maxWidth - 1);
        final List<NodeSubProblem<T>> merge = this.currentLayer.subList(maxWidth - 1, currentLayer.size());
        final T merged = relax.mergeStates(new NodeSubProblemsAsStateIterator<>(merge.iterator()));

        // is there another state in the kept partition having the same state as the merged state ?
        NodeSubProblem<T> node = null;
        boolean fresh = true;
        for (NodeSubProblem<T> n : keep) {
            if (n.state.equals(merged)) {
                node = n;
                fresh = false;
                break;
            }
        }
        // when the merged node is new, set its type to relaxed
        if (node == null) {
            Node newNode = new Node(Double.NEGATIVE_INFINITY);
            newNode.setNodeType(NodeType.RELAXED);
            node = new NodeSubProblem<>(merged, Double.NEGATIVE_INFINITY, newNode);
        }

        // redirect and relax all arcs entering the merged node
        for (NodeSubProblem<T> drop : merge) {
            node.ub = Math.max(node.ub, drop.ub);

            for (Edge e : drop.node.edges) {
                double rcost = relax.relaxEdge(prevLayer.get(e.origin).state, drop.state, merged, e.decision, e.weight);

                double value = saturatedAdd(e.origin.value, rcost);
                e.weight = rcost;
                // if there exists an entring arc with relaxed origin, set the merged node to relaxed
                if (e.origin.getNodeType() == NodeType.RELAXED) {
                    node.node.setNodeType(NodeType.RELAXED);
                }
                node.node.edges.add(e);
                if (value > node.node.value) {
                    node.node.value = value;
                    node.node.best = e;
                }
            }
        }


        // delete the nodes that have been merged
        merge.clear();
        // append the newly merged node if needed
        if (fresh) {
            currentLayer.add(node);
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
    private void branchOn(final NodeSubProblem<T> node, final Decision decision, final Problem<T> problem) {
        T state = problem.transition(node.state, decision);
        double cost = problem.transitionCost(node.state, decision);
        double value = saturatedAdd(node.node.value, cost);

        // when the origin is relaxed, the destination must be relaxed
        Node n = nextLayer.get(state);
        if (n == null) {
            n = new Node(value);
            if (node.node.getNodeType() == NodeType.RELAXED) {
                n.setNodeType(NodeType.RELAXED);
            }
            nextLayer.put(state, n);
        } else {
            if (node.node.getNodeType() == NodeType.RELAXED) {
                n.setNodeType(NodeType.RELAXED);
            }
        }

        Edge edge = new Edge(node.node, decision, cost);
        n.edges.add(edge);
        if (value >= n.value) {
            n.best = edge;
            n.value = value;
        }
    }


    /**
     * Performs a bottom up traversal of the mdd to compute the local bounds
     */
    private void computeLocalBounds() {
        HashSet<Node> current = new HashSet<>();
        HashSet<Node> parent = new HashSet<>();
        parent.addAll(nextLayer.values());

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
                            origin.suffix = Math.max(origin.suffix, saturatedAdd(n.suffix, e.weight));
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

        String nodeStr = String.format(
                "\"%s\nub: %s - value: %s\"",
                node.state,
                df.format(node.ub),
                df.format(node.node.value)
        );

        StringBuilder sb = new StringBuilder();
        sb.append(node.node.hashCode());
        sb.append(" [label=").append(nodeStr);
        if (node.node.getNodeType() == NodeType.RELAXED) {
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
     */
    private void updateBestEdgeColor(int edgeHash) {
        String edgeStr = edgesDotStr.get(edgeHash);
        if (edgeStr != null) {
            edgeStr += ", color=\"#6fb052\", fontcolor=\"#6fb052\"";
            edgesDotStr.replace(edgeHash, edgeStr);
        }
    }

    /**
     * Performs a saturated addition (no overflow)
     */
    private static double saturatedAdd(double a, double b) {
        double sum = a + b;
        if (Double.isInfinite(sum)) {
            return sum > 0 ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
        }
        return sum;
    }

    /**
     * An iterator that transforms the inner subroblems into actual subroblems
     */
    private static final class NodeSubProblemsAsSubProblemsIterator<T> implements Iterator<SubProblem<T>> {
        /**
         * The collection being iterated upon
         */
        private final Iterator<NodeSubProblem<T>> it;
        /**
         * The list of decisions constitutive of the path to root
         */
        private final Set<Decision> ptr;

        /**
         * Creates a new instance
         *
         * @param it  the decorated iterator
         * @param ptr the path to root
         */
        public NodeSubProblemsAsSubProblemsIterator(final Iterator<NodeSubProblem<T>> it, final Set<Decision> ptr) {
            this.it = it;
            this.ptr = ptr;
        }

        @Override
        public boolean hasNext() {
            return it.hasNext();
        }

        @Override
        public SubProblem<T> next() {
            return it.next().toSubProblem(ptr);
        }
    }

    /**
     * An iterator that transforms the inner subroblems into their representing states
     */
    private static final class NodeSubProblemsAsStateIterator<T> implements Iterator<T> {
        /**
         * The collection being iterated upon
         */
        private final Iterator<NodeSubProblem<T>> it;

        /**
         * Creates a new instance
         *
         * @param it the decorated iterator
         */
        public NodeSubProblemsAsStateIterator(final Iterator<NodeSubProblem<T>> it) {
            this.it = it;
        }

        @Override
        public boolean hasNext() {
            return it.hasNext();
        }

        @Override
        public T next() {
            return it.next().state;
        }
    }

    /**
     * This utility class implements a decorator pattern to sort NodeSubProblems by their value then state
     */
    private static final class NodeSubroblemComparator<T> implements Comparator<NodeSubProblem<T>> {
        /**
         * This is the decorated ranking
         */
        private final StateRanking<T> delegate;

        /**
         * Creates a new instance
         *
         * @param delegate the decorated ranking
         */
        public NodeSubroblemComparator(final StateRanking<T> delegate) {
            this.delegate = delegate;
        }

        @Override
        public int compare(NodeSubProblem<T> o1, NodeSubProblem<T> o2) {
            double cmp = o1.node.value - o2.node.value;
            if (cmp == 0) {
                return delegate.compare(o1.state, o2.state);
            } else {
                return Double.compare(o1.node.value, o2.node.value);
            }
        }
    }
}

