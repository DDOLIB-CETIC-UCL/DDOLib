package org.ddolib.ddo.implem.mdd;

import org.ddolib.ddo.core.*;
import org.ddolib.ddo.heuristics.StateRanking;
import org.ddolib.ddo.heuristics.VariableHeuristic;
import org.ddolib.ddo.implem.cache.SimpleCache;
import org.ddolib.ddo.implem.cache.Threshold;
import org.ddolib.ddo.implem.dominance.SimpleDominanceChecker;

import java.sql.SQLOutput;
import java.util.*;
import java.util.Map.Entry;

import static org.apache.commons.lang3.ArrayUtils.isSorted;

/**
 * This class implements the decision diagram as a linked structure.
 * @param <T> the type of state
 * @param <K> the type of key
 */
public final class LinkedDecisionDiagramCache<T,K> implements DecisionDiagramCache<T,K> {
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

    /** A flag to keep track of the fact the MDD was relaxed (some merged occurred) or restricted  (some states were dropped) */
    private boolean exact = true;

    /**
     * The best node in the terminal layer (if it exists at all)
     */
    private Node best = null;

    // --- UTILITY CLASSES -----------------------------------------------

    /**
     * This is an atomic node from the decision diagram. Per-se, it does not
     * hold much interpretable information.
     */
    private static final class Node {
        /**
         * The length of the longest path to this node
         */
        private int value;
        /**
         * The length of the longest suffix of this node (bottom part of a local bound)
         */
        private Integer suffix;
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

        /**
         * The flag to indicate if a node is in exact cutset
         */
        private boolean isInExactCutSet;

        /**
         * The flag to indicate if a node is above the exact cutset
         */
        private boolean isAboveExactCutSet;

        /**
         * The flag to indicate if a node is an exact successor of an exact cutset
         */
        private boolean isSuccessorOfExactCutSet;



        /**
         * Creates a new node
         */
        public Node(final int value) {
            this.value = value;
            this.suffix = null;
            this.best = null;
            this.edges = new ArrayList<>();
            this.type = NodeType.EXACT;
            this.isMarked = false;
            this.isInExactCutSet = false;
            this.isAboveExactCutSet = false;
            this.isSuccessorOfExactCutSet = false;
        }

        /**
         * set the type of the node when different to exact type
         * @param nodeType
         */
        public void setNodeType(final NodeType nodeType) {
            this.type = nodeType;
        }

        /**
         * get the type of the node
         * @return NodeType
         */
        public NodeType getNodeType() {return this.type;}

        @Override
        public String toString() {
            return String.format("Node: value:%d - suffix: %s - best edge: %s - parent edges: %s",
                    value, suffix, best, edges);
        }
    }

    /**
     * Flag to identify the type of node: exact node, relaxed node, marked node, etc ...
     */
    public enum NodeType {
        EXACT, RELAXED;
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
        private int weight;

        /**
         * Creates a new edge between pairs of nodes
         *
         * @param src the source node
         * @param d   the decision that was made when traversing this edge
         * @param w   the weight of the edge
         */
        public Edge(final Node src, final Decision d, final int w) {
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
        private int ub;

        /**
         * Creates a new instance
         */
        public NodeSubProblem(final T state, final int ub, final Node node) {
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

            int locb = Integer.MIN_VALUE;
            if (node.suffix != null) {
                locb = saturatedAdd(node.value, node.suffix);
            }
            ub = Math.min(ub, locb);

            return new SubProblem<>(state, node.value, ub, path);
        }


        @Override
        public String toString() {
            return String.format("%s - ub: %d - type: %s", state, ub, node.type);
        }
    }

    @Override
    public void compile(CompilationInputCache<T,K> input) {
        // make sure we don't have any stale data left
        this.clear();

        // initialize the compilation
        final int maxWidth = input.getMaxWidth();
        final SubProblem<T> residual = input.getResidual();
        final Node root = new Node(residual.getValue());
        this.pathToRoot = residual.getPath();
        this.nextLayer.put(residual.getState(), root);

        // proceed to compilation
        final Problem<T> problem = input.getProblem();
        final Relaxation<T> relax = input.getRelaxation();
        final VariableHeuristic<T> var = input.getVariableHeuristic();
        final NodeSubroblemComparator<T> ranking = new NodeSubroblemComparator<>(input.getStateRanking());
        final SimpleDominanceChecker<T, K> dominance = input.getDominance();
        final SimpleCache<T> cache = input.getCache();
        int bestLb = input.getBestLB();

        final Set<Integer> variables = varSet(input);

        int depth = 0;
        int minDepth = residual.getPath().size();
        int depthCutSet = -1;
        int maxDepth = minDepth + variables.size() - 1;
        Set<NodeSubProblem<T>> currentCutSet = new HashSet<>();
        // list of depth for the current relax compilation of the DD
        ArrayList<Integer> listDepths = new ArrayList<>();
        // the list of NodeSubProblem of the corresponding depth
        ArrayList<ArrayList<NodeSubProblem<T>>> nodeSubProblemPerLayer = new ArrayList<>();
        // the list of Threshold of the corresponding depth
        ArrayList<ArrayList<Threshold>> layersThresholds = new ArrayList<>();
        // list of nodes pruned
        ArrayList<NodeSubProblem<T>> pruned = new ArrayList<>();

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
                if (node.getNodeType() == NodeType.EXACT && dominance.updateDominance(state, depth, node.value)) {
                    continue;
                } else {
                    int rub = saturatedAdd(node.value, input.getRelaxation().fastUpperBound(state, variables));
                    this.currentLayer.add(new NodeSubProblem<>(state, rub, node));
                }
            }

            // prunes the current layer with the current values of the cache
            pruned.clear();
            if (depth > minDepth) {
                for (NodeSubProblem<T> n : this.currentLayer) {
                    if (cache.getLayer(depth).containsKey(n.state) && cache.getThreshold(n.state, depth).isPresent() &&
                            n.node.value <= cache.getThreshold(n.state, depth).get().getValue()) {
                        pruned.add(n);
                    }
                }
            }
            if (!pruned.isEmpty())
                System.out.println("pruned : " + pruned.size());
            this.currentLayer.removeAll(pruned);
            this.nextLayer.clear();

            if (this.currentLayer.isEmpty()) {
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

            if (depth >= 2 && this.currentLayer.size() > maxWidth) {
                switch (input.getCompilationType()) {
                    case Restricted:
                        exact = false;
                        restrict(maxWidth, ranking);
                        break;
                    case Relaxed:
                        if (exact) {
                            exact = false;
                            if (input.getCutSetType() == CutSetType.LastExactLayer) {
                                cutset.addAll(prevLayer.values());
                                if (depthCutSet == -1) {
                                    depthCutSet = depth - 1;
                                }
                            }
                        }
                        relax(maxWidth, ranking, relax);
                        break;
                    case Exact:
                        /* nothing to do */
                        break;
                }
            }

            for (NodeSubProblem<T> n : this.currentLayer) {
                int lb = input.getBestLB();
                if (n.ub <= input.getBestLB()) {
                    continue;
                } else {
                    final Iterator<Integer> domain = problem.domain(n.state, nextVar);
                    while (domain.hasNext()) {
                        final int val = domain.next();
                        final Decision decision = new Decision(nextVar, val);

                        branchOn(n, decision, problem);
                    }
                }
            }

            // Compute cutset: exact parent nodes of relaxed nodes of the current nodes are put in the cutset
            if (input.getCutSetType() == CutSetType.Frontier && input.getCompilationType() == CompilationType.Relaxed && !exact && depth >= 2) {
                for (NodeSubProblem<T> n : this.currentLayer) {
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

            // Compute the list of sub-problems per layer with the current layer
            // Initialize the list of thresholds per layer to their default values

            if (input.getCompilationType() == CompilationType.Relaxed) {
                listDepths.add(depth + minDepth);
                nodeSubProblemPerLayer.add(new ArrayList<>());
                layersThresholds.add(new ArrayList<>());
//                System.out.println(this.currentLayer   + " --->  " + depth);
                for (NodeSubProblem<T> n : this.currentLayer) {
                    nodeSubProblemPerLayer.get(depth).add(n);
                    layersThresholds.get(depth).add(new Threshold(Integer.MAX_VALUE, false));
                }
            }

            depth += 1;
        }
        if (input.getCompilationType() == CompilationType.Relaxed && input.getCutSetType() == CutSetType.Frontier) {
            cutset.addAll(currentCutSet);
        }

        // finalize: find best
        for (Node n : nextLayer.values()) {
            if (best == null || n.value > best.value) {
                best = n;
            }
        }


        // Compute the local bounds of the nodes in the mdd *iff* this is a relaxed mdd
        if (input.getCompilationType() == CompilationType.Relaxed) {
            computeLocalBounds();
        }

        // Compute the threshold of every exact node in the relaxed DD and update the cache
        if (input.getCompilationType() == CompilationType.Relaxed) {
            for (NodeSubProblem<T> n : cutset) {
                if (n.node.isMarked) {
                    n.node.isInExactCutSet = true;
                }
            }
            for (int i = 0; i < depthCutSet; i++) {
                for (NodeSubProblem<T> n: nodeSubProblemPerLayer.get(i)) {
                    if (n.node.getNodeType() == NodeType.EXACT) {
                        n.node.isAboveExactCutSet = true;
                    }
                }
            }
            for (int i = depthCutSet+1; i < nodeSubProblemPerLayer.size(); i++) {
                for (NodeSubProblem<T> n: nodeSubProblemPerLayer.get(i)) {
                    if (n.node.getNodeType() == NodeType.EXACT) {
                        n.node.isSuccessorOfExactCutSet = true;
                    }
                }
            }
            computeAndUpdateThreshold(cache, listDepths, nodeSubProblemPerLayer, layersThresholds, bestLb, input.getCutSetType());
        }
    }

    @Override
    public boolean isExact() {
        return exact;
        //return !lelWasSet;
    }

    @Override
    public Optional<Integer> bestValue() {
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
            Set<Decision> sol = new HashSet<>();
            sol.addAll(pathToRoot);

            Edge eb = best.best;
            while (eb != null) {
                sol.add(eb.decision);
                eb = eb.origin == null ? null : eb.origin.best;
            }
            return Optional.of(sol);
        }
    }

    @Override
    public Iterator<SubProblem<T>> exactCutset() {
        return new NodeSubProblemsAsSubProblemsIterator<>(cutset.iterator(), pathToRoot);
    }


    // --- UTILITY METHODS -----------------------------------------------
    private Set<Integer> varSet(final CompilationInputCache<T,K> input) {
        final HashSet<Integer> set = new HashSet<>();
        for (int i = 0; i < input.getProblem().nbVars(); i++) {
            set.add(i);
        }

        for (Decision d : input.getResidual().getPath()) {
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
    }

    /**
     * Performs a restriction of the current layer.
     *
     * @param maxWidth the maximum tolerated layer width
     * @param ranking  a ranking that orders the nodes from the most promising (greatest)
     *                 to the least promising (lowest)
     */
    private void restrict(final int maxWidth, final NodeSubroblemComparator<T> ranking) {
//        System.out.println("before restriction : " +  this.currentLayer);
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
//        System.out.println("before relaxation : " +  this.currentLayer);
        this.currentLayer.sort(ranking.reversed());
//        System.out.println("before relaxation : " +  this.currentLayer);

        final List<NodeSubProblem<T>> keep = this.currentLayer.subList(0, maxWidth - 1);
        final List<NodeSubProblem<T>> merge = this.currentLayer.subList(maxWidth - 1, this.currentLayer.size());
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
            Node newNode = new Node(Integer.MIN_VALUE);
            newNode.setNodeType(NodeType.RELAXED);
            node = new NodeSubProblem<>(merged, Integer.MIN_VALUE, newNode);
        }

        // redirect and relax all arcs entering the merged node
        for (NodeSubProblem<T> drop : merge) {
            node.ub = Math.max(node.ub, drop.ub);

            for (Edge e : drop.node.edges) {
                int rcost = relax.relaxEdge(prevLayer.get(e.origin).state, drop.state, merged, e.decision, e.weight);

                int value = saturatedAdd(e.origin.value, rcost);
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
            this.currentLayer.add(node);
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
        int cost = problem.transitionCost(node.state, decision);
        int value = saturatedAdd(node.node.value, cost);

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
            n.suffix = 0;
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
     * perform the bottom up traversal of the mdd to compute and update the cache
     */
    private void computeAndUpdateThreshold(SimpleCache<T> simpleCache, ArrayList<Integer> listDepth, ArrayList<ArrayList<NodeSubProblem<T>>> nodePerLayer, ArrayList<ArrayList<Threshold>> currentCache, int lb, CutSetType cutSetType) {
        for (int j = listDepth.size()-1; j >= 0; j--) {
            int depth = listDepth.get(j);
            for (int i = 0; i < nodePerLayer.get(j).size(); i++) {
                NodeSubProblem<T> sub = nodePerLayer.get(j).get(i);
                if (simpleCache.getLayer(depth).containsKey(sub.state) && simpleCache.getLayer(depth).get(sub.state).isPresent() &&
                        sub.node.value <= simpleCache.getLayer(depth).get(sub.state).get().getValue()) {
                    int value = simpleCache.getLayer(depth).get(sub.state).get().getValue();
                    currentCache.get(j).get(i).setValue(value);
                }
                else {
                    if (sub.ub <= lb) {
                        int rub = saturatedDiff(sub.ub, sub.node.value);
                        int value = saturatedDiff(lb, rub);
                        currentCache.get(j).get(i).setValue(value);
                    } else if (sub.node.isInExactCutSet) {
                        if (sub.node.suffix != null && saturatedAdd(sub.node.value, sub.node.suffix) <= lb) {
                            int value = Math.min(currentCache.get(j).get(i).getValue(), saturatedDiff(lb, sub.node.suffix));
                            currentCache.get(j).get(i).setValue(value);
                        } else {
                            currentCache.get(j).get(i).setValue(sub.node.value);
                        }
                    }
                    if (sub.node.getNodeType() == NodeType.EXACT) {
                        if (cutSetType == CutSetType.LastExactLayer) {
                            if ((sub.node.isSuccessorOfExactCutSet && sub.node.isMarked)  || (sub.node.isInExactCutSet && sub.node.value == currentCache.get(j).get(i).getValue())) {
                                currentCache.get(j).get(i).setExplored(false);
                            } else if (sub.node.isAboveExactCutSet || (sub.node.isInExactCutSet && sub.node.value < currentCache.get(j).get(i).getValue())) {
                                currentCache.get(j).get(i).setExplored(true);
                            }

                            if (currentCache.get(j).get(i).isExplored()) {
                                simpleCache.getLayer(depth).update(sub.state, currentCache.get(j).get(i));
                            }
                        }
                        if (cutSetType == CutSetType.Frontier) {
                            if (!sub.node.isInExactCutSet) {
                                currentCache.get(j).get(i).setExplored(true);
                            }
                            if (currentCache.get(j).get(i).isExplored()) {
                                simpleCache.getLayer(depth).update(sub.state, currentCache.get(j).get(i));
                            }
                        }
                    }
                }
                for (Edge e : sub.node.edges) {
                    Node origin = e.origin;
                    int index = -1;
                    for (int k = 0; k < nodePerLayer.get(j-1).size(); k++) {
                        if (nodePerLayer.get(j-1).get(k).node.equals(origin)) {
                            index = k;
                            break;
                        }
                    }
                    int value = Math.min(currentCache.get(j-1).get(index).getValue(), saturatedDiff(currentCache.get(j).get(i).getValue(), e.weight));
                    currentCache.get(j-1).get(index).setValue(value);
                }
            }
        }
    }

    /**
     * Performs a saturated addition (no overflow)
     */
    private static final int saturatedAdd(int a, int b) {
        long sum = (long) a + (long) b;
        sum = sum >= Integer.MAX_VALUE ? Integer.MAX_VALUE : sum;
        sum = sum <= Integer.MIN_VALUE ? Integer.MIN_VALUE : sum;
        return (int) sum;
    }

    /**
     * Performs a saturated difference (no overflow)
     */
    private static final int saturatedDiff(int a, int b) {
        long diff = (long) a - (long) b;
        diff = diff >= Integer.MAX_VALUE ? Integer.MAX_VALUE : diff;
        diff = diff <= Integer.MIN_VALUE ? Integer.MIN_VALUE : diff;
        return (int) diff;
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
            int cmp = o1.node.value - o2.node.value;
            if (cmp == 0) {
                return delegate.compare(o1.state, o2.state);
            } else {
                return cmp;
            }
        }
    }
}
