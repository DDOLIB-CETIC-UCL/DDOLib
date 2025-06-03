package org.ddolib.ddo.implem.mdd;

import org.ddolib.ddo.core.*;
import org.ddolib.ddo.heuristics.StateRanking;
import org.ddolib.ddo.heuristics.VariableHeuristic;

import java.util.*;
import java.util.Map.Entry;

/**
 * This class implements the decision diagram as a linked structure. 
 */
public final class LinkedDecisionDiagram<T> implements DecisionDiagram<T> {
    /** The list of decisions that have led to the root of this DD */
    private Set<Decision> pathToRoot = Collections.emptySet();
    /** All the nodes from the previous layer */
    private HashMap<Node, NodeSubProblem<T>> prevLayer = new HashMap<>();
    /** All the (subproblems) nodes from the previous layer -- That is, all nodes that will be expanded */
    private List<NodeSubProblem<T>> currentLayer = new ArrayList<>();
    /** All the nodes from the next layer */
    private HashMap<T, Node> nextLayer = new HashMap<T, Node>();
    /** All the nodes from the last exact layer cutset */
    private List<NodeSubProblem<T>> lel = new ArrayList<>();
    /** A flag to keep track of the fact that LEL might be empty albeit not set */
    private boolean lelWasSet = false;
    /** The best node in the terminal layer (if it exists at all) */
    private Node best = null;

    // --- UTILITY CLASSES -----------------------------------------------
    /**
     * This is an atomic node from the decision diagram. Per-se, it does not 
     * hold much interpretable information.
     */
    private static final class Node {
        /** The length of the longest path to this node */
        private int value;
        /** The length of the longest suffix of this node (bottom part of a local bound) */
        private Integer suffix;
        /** The edge terminating the longest path to this node */
        private Edge best;
        /** The list of edges leading to this node */
        private List<Edge> edges;

        /** Creates a new node */
        public Node(final int value) {
            this.value  = value;
            this.suffix = null;
            this.best   = null;
            this.edges  = new ArrayList<>();
        }
    }
    /**
     * This is an edge that connects two nodes from the decision diagram
     */
    private static final class Edge {
        /** The source node of this arc */
        private Node origin;
        /** The decision that was made when traversing this arc */
        private Decision decision;
        /** The weight of the arc */
        private int weight;

        /**
         * Creates a new edge between pairs of nodes
         * 
         * @param src the source node
         * @param d the decision that was made when traversing this edge
         * @param w the weight of the edge
         */
        public Edge(final Node src, final Decision d, final int w) {
            this.origin = src;
            this.decision = d;
            this.weight = w;
        }
    }
    /**
     * This class encapsulates the association of a node with its state and 
     * associated rough upper bound.
     * 
     * This class essentially serves two purposes: 
     * 
     * - associate a node with a state during the compilation (and allow to 
     *   eagerly forget about the given state, which allows to save substantial
     *   amounts of RAM while compiling the DD).
     * 
     * - turn an MDD node from the exact cutset into a subproblem which is used
     *   by the API.
     */
    private static final class NodeSubProblem<T> {
        /** The state associated to this node */
        private final T state;
        /** The actual node from the graph of decision diagrams */
        private final Node node;
        /** The upper bound associated with this node (if state were the root) */
        private int ub;

        /** Creates a new instance */
        public NodeSubProblem(final T state, final int ub, final Node node){
            this.state = state;
            this.ub    = ub;
            this.node  = node;
        }

        /** @return Turns this association into an actual subproblem */
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
        public boolean equals(final Object o) {
            return ((NodeSubProblem<T>) o).state == this.state;
        }

        @Override
        public int hashCode() {
            return this.state.hashCode();
        }
    }

    @Override
    public void compile(CompilationInput<T> input) {
        // make sure we don't have any stale data left
        this.clear();

        // initialize the compilation
        final int maxWidth           = input.getMaxWidth();
        final SubProblem<T> residual = input.getResidual();
        final Node root              = new Node(residual.getValue());
        this.pathToRoot              = residual.getPath();
        this.nextLayer.put(residual.getState(), root);

        // proceed to compilation
        final Problem<T> problem       = input.getProblem();
        final Relaxation<T> relax      = input.getRelaxation();
        final VariableHeuristic<T> var = input.getVariableHeuristic();
        final NodeSubroblemComparator<T> ranking = new NodeSubroblemComparator<>(input.getStateRanking());

        final Set<Integer> variables = varSet(input);
        //
        int depth = 0;

        while (!variables.isEmpty()) {
            // System.out.println("****************");
            // System.out.println("depth: " + depth);
            Integer nextvar = var.nextVariable(variables, nextLayer.keySet().iterator());
            // change the layer focus: what was previously the next layer is now
            // becoming the current layer
            this.prevLayer.clear();
            for (NodeSubProblem<T> n : this.currentLayer) {
                this.prevLayer.put(n.node, n);
            }
            this.currentLayer.clear();

            for (Entry<T, Node> e : this.nextLayer.entrySet()) {
                T state   = e.getKey();
                Node node = e.getValue();

                int rub  = saturatedAdd(node.value, input.getRelaxation().fastUpperBound(state, variables));
                this.currentLayer.add(new NodeSubProblem<>(state, rub, node));
            }
            this.nextLayer.clear();

            /*System.out.println("Layer before relaxation: ");
            for (NodeSubProblem<T> n : this.currentLayer) {
                System.out.print(n.state + ", ");
            }
            System.out.println();*/

            if (currentLayer.isEmpty()) {
                // there is no feasible solution to this subproblem, we can stop the compilation here
                return;
            }

            if (nextvar == null) {
                // Some variables simply can't be assigned
                clear();
                return;
            } else {
                variables.remove(nextvar);
            }

            // System.out.println("Width: " + currentLayer.size());


            // If the current layer is too large, we need to shrink it down. 
            // Whether this shrinking down means that we want to perform a restriction
            // or a relaxation depends on the type of compilation which has been 
            // requested from this decision diagram  
            //
            // IMPORTANT NOTE:
            // The check is on depth 2 because the method maybeSaveLel() saves the parent
            // of the current layer if a LEL is to be remembered. In order to be sure
            // to make progress, we must be certain to develop AT LEAST one layer per 
            // mdd compiled otherwise the LEL is going to be the root of this MDD (and
            // we would be stuck in an infinite loop)
            //System.out.println("Depth:" + depth);
            // System.out.println("Width: " + currentLayer.size());
            if (depth >= 2 && currentLayer.size() > maxWidth) {
                switch (input.getCompilationType()) {
                    case Restricted:
                        maybeSaveLel();
                        restrict(maxWidth, ranking);
                        break;
                    case Relaxed:
                        maybeSaveLel();
                        switch (input.getRelaxType()) {
                            case Cost:
                                relax(maxWidth, ranking, relax);
                                break;
                            case Cluster:
                                relaxCluster(maxWidth, input.getStateRanking(), relax);
                                break;
                        }
                        break;
                    case Exact: 
                        /* nothing to do */
                        break;
                }
            }

            /*System.out.println("Layer after relaxation: ");
            for (NodeSubProblem<T> n : this.currentLayer) {
                System.out.print(n.state + ", ");
            }
            System.out.println();*/

            for (NodeSubProblem<T> n : currentLayer) {
                // System.out.println("State: " + n.state);
                if (n.ub <= input.getBestLB()) {
                    continue;
                } else {
                    final Iterator<Integer> domain = problem.domain(n.state, nextvar);
                    // System.out.print("domain: ");
                    while (domain.hasNext()) {
                        final int val           = domain.next();
                        final Decision decision = new Decision(nextvar, val);
                        // System.out.print(decision.val() + ", ");
                        branchOn(n, decision, problem);
                    }
                    // System.out.println();
                }
            }
            // System.out.println("********************************");
            depth += 1;
        }

        // finalize: find best
        for (Node n : nextLayer.values()) {
            if (best == null || n.value > best.value) {
                best = n;
            }
        }

        //System.out.println("Best: " + best.value);

        // Compute the local bounds of the nodes in the mdd *iff* this is a relaxed mdd
        if (input.getCompilationType() == CompilationType.Relaxed) {
            computeLocalBounds();
        }
    }

    @Override
    public boolean isExact() {
        return !lelWasSet;
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
        return new NodeSubProblemsAsSubProblemsIterator<>(lel.iterator(), pathToRoot);
    }
    
    // --- UTILITY METHODS -----------------------------------------------
    private Set<Integer> varSet(final CompilationInput<T> input) {
        final HashSet<Integer> set = new HashSet<>();
        for (int i = 0; i < input.getProblem().nbVars(); i++) {
            set.add(i);
        }

        for (Decision d : input.getResidual().getPath()) {
            set.remove(d.var());
        }
        return set;
    }
    /** Reset the state of this MDD. This way it can easily be reused */
    private void clear() {
        pathToRoot = Collections.emptySet();
        prevLayer.clear();
        currentLayer.clear();
        nextLayer.clear();
        lel.clear();
        lelWasSet = false;
        best = null;
    }
    /** Saves the last exact layer cutset if needed */
    private void maybeSaveLel() {
        if (!lelWasSet) {
            lel.addAll(prevLayer.values());
        }
        lelWasSet = true;
    }
    /**
     * Performs a restriction of the current layer. 
     * 
     * @param maxWidth the maximum tolerated layer width
     * @param ranking a ranking that orders the nodes from the most promising (greatest)
     *  to the least promising (lowest) 
     */
    private void restrict(final int maxWidth, final NodeSubroblemComparator<T> ranking) {
        this.currentLayer.sort(ranking.reversed());
        this.currentLayer.subList(maxWidth, this.currentLayer.size()).clear(); // truncate
    }

    private class STNode {
        public final NodeSubProblem<T> A;
        public final NodeSubProblem<T> B;
        public final T merged;

        public STNode(NodeSubProblem<T> A, NodeSubProblem<T> B, T merged) {
            this.A = A;
            this.B = B;
            this.merged = merged;
        }
    }

    private void redirectArcs(NodeSubProblem<T> merged, Set<NodeSubProblem<T>> toMerge, final Relaxation<T> relax) {
        // redirect and relax all arcs entering the merged node
        for (NodeSubProblem<T> drop : toMerge) {
            merged.ub = Math.max(merged.ub, drop.ub);

            for (Edge e : drop.node.edges) {
                int rcost = relax.relaxEdge(prevLayer.get(e.origin).state, drop.state, merged.state, e.decision, e.weight);

                int value = saturatedAdd(e.origin.value, rcost);
                e.weight  = rcost;

                merged.node.edges.add(e);
                if (value > merged.node.value) {
                    merged.node.value = value;
                    merged.node.best  = e;
                }
            }
        }
    }

    private boolean shareParents(NodeSubProblem<T> a, NodeSubProblem<T> b) {
        Set<Node> parentA = new HashSet<>();
        Set<Node> parentB = new HashSet<>();
        for (Edge e : a.node.edges) {
            parentA.add(e.origin);
        }
        for (Edge e : b.node.edges) {
            parentB.add(e.origin);
        }
        parentA.retainAll(parentB);
        return !parentA.isEmpty();
    }

    /**
     * Performs a relaxation of the current layer using clustering to determine nodes to merge
     * @param maxWidth the maximum tolerated layer width
     * @param ranking a ranking that orders the nodes from the most promising (greatest)
     *  to the least promising (lowest)
     * @param relax the relaxation operators which we will use to merge nodes
     */
    private void relaxCluster(final int maxWidth, final StateRanking<T> ranking, final Relaxation<T> relax) {

        NodeSubroblemComparator<T> rankingComparator = new NodeSubroblemComparator<>(ranking);

        // To reduce memory consumption and computational cost, the maxWidth/2 best state are not merged
        // TODO: find a smarter and more scalable way to determine which nodes should be considered for merging
        this.currentLayer.sort(rankingComparator.reversed());
        int nbrKept = (int) Math.ceil(maxWidth* 2.0/4.0);
        /// final List<NodeSubProblem<T>> keep  = this.currentLayer.subList(0, nbrKept);
        final List<NodeSubProblem<T>> merge = this.currentLayer.subList(nbrKept, currentLayer.size());
        Set<NodeSubProblem<T>> currentLayerSet = new HashSet<>(merge);
        // final List<NodeSubProblem<T>> keep  = this.currentLayer.subList(0, nbrKept);
        // final List<NodeSubProblem<T>> merge = this.currentLayer.subList(nbrKept, currentLayer.size());*/

        // The pq contains each potential pair of clusters
        // the problem is that the size of the layer can quickly explode, quickly create memory issues
        // Maybe all potential merge don't need to be considered

        PriorityQueue<STNode> pq = new PriorityQueue<>((a,b) -> ranking.compare(a.merged, b.merged));
        T mergedState;
        for (int i = 0; i < merge.size() - 1; i++) {
            for (int j = i + 1; j < merge.size(); j++) {
                mergedState = relax.mergeStates(Arrays.asList(merge.get(i).state, merge.get(j).state).iterator());
                pq.add(new STNode(merge.get(i),
                        merge.get(j),
                        mergedState));
            }
        }
        merge.clear();
        while ((currentLayerSet.size()+ nbrKept > maxWidth) && !pq.isEmpty()) {
            STNode current = pq.poll();
            assert current != null;

            // if the merging became unvalid because of previous merge we skip it
            if (!currentLayerSet.contains(current.A) || !currentLayerSet.contains(current.B)) {
                continue;
            }

            Set<NodeSubProblem<T>> mergeSet;
            boolean newState = false;

            currentLayerSet.remove(current.A);
            currentLayerSet.remove(current.B);

            NodeSubProblem<T> newNode = null;
            boolean fresh = true;
            for (NodeSubProblem<T> node: currentLayerSet) {
                if (node.state == current.merged) {
                    newNode = node;
                    fresh = false;
                    break;
                }
            }
            if (newNode == null) newNode = new NodeSubProblem<>(current.merged, Integer.MIN_VALUE, new Node(Integer.MIN_VALUE));
            redirectArcs(newNode, Set.of(current.A, current.B), relax);

            if (fresh) {
                assert !currentLayerSet.contains(newNode);
                for (NodeSubProblem<T> node : currentLayerSet) {
                    T newMerged = relax.mergeStates(Arrays.asList(current.merged, node.state).iterator());
                    pq.add(new STNode(newNode, node, newMerged));
                }
            }
            currentLayerSet.add(newNode);
        }
        // currentLayer.clear();
        currentLayer.addAll(currentLayerSet);
    }

    /**
     * Performs a restriction of the current layer. 
     * 
     * @param maxWidth the maximum tolerated layer width
     * @param ranking a ranking that orders the nodes from the most promising (greatest)
     *  to the least promising (lowest) 
     * @param relax the relaxation operators which we will use to merge nodes
     */
    private void relax(final int maxWidth, final NodeSubroblemComparator<T> ranking, final Relaxation<T> relax) {
        this.currentLayer.sort(ranking.reversed());
        final List<NodeSubProblem<T>> keep  = this.currentLayer.subList(0, maxWidth-1);
        final List<NodeSubProblem<T>> merge = this.currentLayer.subList(maxWidth-1, currentLayer.size());
        /*System.out.println("Merging states: ");
        for (NodeSubProblem<T> n : merge) {
            System.out.print(n.state + ", ");
        }
        System.out.println();*/

        final T merged = relax.mergeStates(new NodeSubProblemsAsStateIterator<>(merge.iterator()));
        // System.out.println("merged: " + merged);

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
        if (node == null) {
            node = new NodeSubProblem<>(merged, Integer.MIN_VALUE, new Node(Integer.MIN_VALUE));
        }

        // redirect and relax all arcs entering the merged node
        for (NodeSubProblem<T> drop : merge) {
            node.ub = Math.max(node.ub, drop.ub);
            
            for (Edge e : drop.node.edges) {
                int rcost = relax.relaxEdge(prevLayer.get(e.origin).state, drop.state, merged, e.decision, e.weight);

                int value = saturatedAdd(e.origin.value, rcost);
                e.weight  = rcost;

                node.node.edges.add(e);
                if (value > node.node.value) {
                    node.node.value = value;
                    node.node.best  = e;
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
     * @param node the origin of the transition
     * @param decision the decision being made
     * @param problem the problem that defines the transition and transition cost functions
     */
    private void branchOn(final NodeSubProblem<T> node, final Decision decision, final Problem<T> problem) {
        T state  = problem.transition(node.state, decision);
        int cost = problem.transitionCost(node.state, decision);
        int value= saturatedAdd(node.node.value, cost);

        Node n   = nextLayer.get(state);
        if (n == null) {
            n = new Node(value);
            nextLayer.put(state, n);
        }

        Edge edge = new Edge(node.node, decision, cost);
        n.edges.add(edge);

        if (value >= n.value) {
            n.best = edge;
            n.value= value;
        }
    }
    
    /** Performs a bottom up traversal of the mdd to compute the local bounds */
    private void computeLocalBounds() {
        HashSet<Node> current = new HashSet<>();
        HashSet<Node> parent  = new HashSet<>();
        parent.addAll(nextLayer.values());

        for (Node n : parent) {
            n.suffix = 0;
        }

        while (!parent.isEmpty()) {
            HashSet<Node> tmp = current;
            current = parent;
            parent  = tmp;
            parent.clear();

            for (Node n : current) {
                for (Edge e : n.edges) {
                    // Note: we might want to do something and stop as soon as the lel has been reached
                    Node origin = e.origin;
                    parent.add(origin);

                    if (origin.suffix == null) {
                        origin.suffix = saturatedAdd(n.suffix, e.weight);
                    } else {
                        origin.suffix = Math.max(origin.suffix, saturatedAdd(n.suffix, e.weight));
                    }
                }
            }
        }
    }

    /** Performs a saturated addition (no overflow) */
    private static final int saturatedAdd(int a, int b) {
        long sum = (long) a + (long) b;
        sum = sum >= Integer.MAX_VALUE ? Integer.MAX_VALUE : sum;
        sum = sum <= Integer.MIN_VALUE ? Integer.MIN_VALUE : sum;
        return (int) sum;
    }

    /** An iterator that transforms the inner subroblems into actual subroblems */
    private static final class NodeSubProblemsAsSubProblemsIterator<T> implements Iterator<SubProblem<T>> {
        /** The collection being iterated upon */
        private final Iterator<NodeSubProblem<T>> it;
        /** The list of decisions constitutive of the path to root */
        private final Set<Decision> ptr;
        /**
         * Creates a new instance
         * @param it the decorated iterator
         * @param ptr the path to root
         */
        public NodeSubProblemsAsSubProblemsIterator(final Iterator<NodeSubProblem<T>> it, final Set<Decision> ptr) {
            this.it = it;
            this.ptr= ptr;
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
    /** An iterator that transforms the inner subroblems into their representing states */
    private static final class NodeSubProblemsAsStateIterator<T> implements Iterator<T> {
        /** The collection being iterated upon */
        private final Iterator<NodeSubProblem<T>> it;
        /**
         * Creates a new instance
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

    /** This utility class implements a decorator pattern to sort NodeSubProblems by their value then state */
    private static final class NodeSubroblemComparator<T> implements Comparator<NodeSubProblem<T>>{
        /** This is the decorated ranking */
        private final StateRanking<T> delegate;
        
        /** 
         * Creates a new instance
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
