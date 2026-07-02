package org.ddolib.nolayer.solving.ddo.core.mdd;

import org.ddolib.common.cache.Cache;
import org.ddolib.common.cache.Threshold;
import org.ddolib.common.compilation.CompilationType;
import org.ddolib.common.mdd.DecisionDiagram;
import org.ddolib.layered.solving.ddo.core.Decision;
import org.ddolib.layered.solving.ddo.core.SubProblem;
import org.ddolib.nolayer.modeling.DdoModel;
import org.ddolib.nolayer.modeling.NoLayerDominanceChecker;
import org.ddolib.nolayer.modeling.Problem;

import java.util.*;
import java.util.stream.Collectors;

import static org.ddolib.util.MathUtil.saturatedDiff;

public class NoLayerDecisionDiagram<T> implements DecisionDiagram<T> {

    private final DdoModel<T> model;
    private final Problem<T> problem;
    private final SubProblem<T> rootSubProblem;
    private final CompilationType type;
    private final int maxWidth;
    private final double primalBound;
    private final Optional<Cache<T>> cache;
    private final Map<T, Node<T>> stateMap = new HashMap<>();
    private final Node<T> rootNode;
    private Node<T> targetNode = null;
    private boolean exact = true;

    public NoLayerDecisionDiagram(DdoModel<T> model, SubProblem<T> rootSubProblem,
                                  CompilationType type, int maxWidth, double primalBound,
                                  Optional<Cache<T>> cache) {
        this.model = model;
        this.problem = model.problem();
        this.rootSubProblem = rootSubProblem;
        this.type = type;
        this.maxWidth = maxWidth;
        this.primalBound = primalBound;
        this.cache = cache;

        this.rootNode = makeNode(rootSubProblem.getState(), true);
        this.rootNode.layer = rootSubProblem.getPath().size();
        this.rootNode.bound = rootSubProblem.getValue();
    }

    private Node<T> makeNode(T state, boolean pExact) {
        Node<T> node = stateMap.get(state);
        if (node != null) {
            node.isExact &= pExact;
            return node;
        }
        node = new Node<>(state);
        node.isExact = pExact;
        node.bound = Double.POSITIVE_INFINITY; // DDOLib Minimizes
        stateMap.put(state, node);
        return node;
    }

    private double saturatedAdd(double a, double b) {
        if (a == Double.POSITIVE_INFINITY || b == Double.POSITIVE_INFINITY) return Double.POSITIVE_INFINITY;
        if (a == Double.NEGATIVE_INFINITY || b == Double.NEGATIVE_INFINITY) return Double.NEGATIVE_INFINITY;
        return a + b;
    }

    private boolean isBetter(double obj1, double obj2) {
        return obj1 < obj2;
    }

    private boolean isBetterEQ(double obj1, double obj2) {
        return obj1 <= obj2;
    }

    @Override
    public void compile() {
        if (type == CompilationType.Relaxed) {
            compileRelaxed();
        } else {
            compileRestricted();
        }
    }

    private void compileRelaxed() {
        PriorityQueue<Node<T>> qn = new PriorityQueue<>(Comparator.comparingInt(n -> n.layer));
        qn.add(rootNode);

        List<Node<T>> currentLayerNodes = new ArrayList<>();

        while (!qn.isEmpty()) {
            Node<T> p = qn.poll();
            currentLayerNodes.add(p);

            if (qn.isEmpty() || qn.peek().layer != p.layer) {
                reduceRelaxed(currentLayerNodes);
                expandLayer(currentLayerNodes, qn);
                currentLayerNodes.clear();
            }
        }

        if (targetNode != null) {
            computeBestBackward();
            updateCache();
        }
    }

    private void compileRestricted() {
        PriorityQueue<Node<T>> qn = new PriorityQueue<>(Comparator.comparingInt(n -> n.layer));
        qn.add(rootNode);

        List<Node<T>> currentLayerNodes = new ArrayList<>();

        while (!qn.isEmpty()) {
            Node<T> p = qn.poll();
            currentLayerNodes.add(p);

            if (qn.isEmpty() || qn.peek().layer != p.layer) {
                reduceRestricted(currentLayerNodes);
                expandLayer(currentLayerNodes, qn);
                currentLayerNodes.clear();
            }
        }
    }

    private void expandLayer(List<Node<T>> layerNodes, PriorityQueue<Node<T>> qn) {
        NoLayerDominanceChecker<T> dominance = model.dominance();
        for (Node<T> n : layerNodes) {
            if (cache.isPresent()) {
                Optional<org.ddolib.common.cache.Threshold> th = cache.get().getThreshold(n.state, n.layer);
                if (th.isPresent() && n.bound >= th.get().getValue()) {
                    continue; // Pruned by cache
                }
            }
            if (n.isSink(problem)) {
                if (targetNode == null) targetNode = n;
                else if (targetNode != n) {
                    targetNode = mergeTargetNodes(targetNode, n);
                }
                continue;
            }

            Iterator<Integer> labels = problem.domain(n.state);
            while (labels.hasNext()) {
                int l = labels.next();
                T nextState = problem.transition(n.state, l);
                double cost = problem.transitionCost(n.state, l);
                double ep = saturatedAdd(n.bound, cost);
                double flb = model.lowerBound().fastLowerBound(nextState);
                if (saturatedAdd(ep, flb) >= primalBound) {
                    continue;
                }

                Node<T> child = stateMap.get(nextState);
                boolean newNode = (child == null);

                if (newNode) {
                    if (dominance.updateDominance(nextState, ep)) {
                        continue; // Dominated!
                    }
                }

                child = makeNode(nextState, n.isExact);

                Edge<T> e = new Edge<>(n, child, l, cost);
                n.outEdges.add(e);
                child.inEdges.add(e);

                if (isBetter(ep, child.bound)) {
                    child.bound = ep;
                    child.bestParentEdge = e;
                }

                int newLayer = Math.max(child.layer, n.layer + 1);
                if (newLayer > child.layer) {
                    if (!newNode) qn.remove(child);
                    child.layer = newLayer;
                    qn.add(child);
                } else if (newNode) {
                    qn.add(child);
                }
            }
        }
    }

    private Node<T> mergeTargetNodes(Node<T> t1, Node<T> t2) {
        // Since it's target, we can just keep t1 and move edges
        for (Edge<T> e : t2.inEdges) {
            e.destination = t1;
            t1.inEdges.add(e);
        }
        if (isBetter(t2.bound, t1.bound)) {
            t1.bound = t2.bound;
            t1.bestParentEdge = t2.bestParentEdge;
        }
        t1.isExact &= t2.isExact;
        stateMap.remove(t2.state);
        return t1;
    }

    private void reduceRelaxed(List<Node<T>> layerNodes) {
        if (layerNodes.size() <= maxWidth) return;
        exact = false;

        List<T> toMergeStates = layerNodes.stream().map(n -> n.state).collect(Collectors.toList());
        List<Double> toMergeBounds = layerNodes.stream().map(n -> saturatedAdd(n.bound, model.lowerBound().fastLowerBound(n.state))).collect(Collectors.toList());
        List<List<T>> grouped = model.relaxStrategy().defineClusters(toMergeStates, toMergeBounds, maxWidth);

        int originalLayer = layerNodes.get(0).layer;
        layerNodes.clear();

        for (List<T> states : grouped) {
            T mergedState = model.relaxation().merge(states);

            List<Edge<T>> allInEdges = new ArrayList<>();
            double bestBound = Double.POSITIVE_INFINITY;
            Edge<T> bestEdge = null;

            boolean allExact = true;
            for (T sp : states) {
                Node<T> oldNode = stateMap.remove(sp);
                if (oldNode != null) {
                    allExact &= oldNode.isExact;
                    allInEdges.addAll(oldNode.inEdges);
                    if (isBetter(oldNode.bound, bestBound)) {
                        bestBound = oldNode.bound;
                        bestEdge = oldNode.bestParentEdge;
                    }
                }
            }

            // It is exact if the cluster size is 1 and the original node was exact
            boolean isExactNode = (states.size() == 1) && allExact;
            Node<T> mergedNode = makeNode(mergedState, isExactNode);
            mergedNode.layer = originalLayer;

            for (Edge<T> e : allInEdges) {
                e.destination = mergedNode;
                mergedNode.inEdges.add(e);
            }

            if (isBetter(bestBound, mergedNode.bound)) {
                mergedNode.bound = bestBound;
                mergedNode.bestParentEdge = bestEdge;
            }

            layerNodes.add(mergedNode);
        }
    }

    private void reduceRestricted(List<Node<T>> layerNodes) {
        if (layerNodes.size() <= maxWidth) return;
        exact = false;

        layerNodes.sort((o1, o2) -> {
            double v1 = saturatedAdd(o1.bound, model.lowerBound().fastLowerBound(o1.state));
            double v2 = saturatedAdd(o2.bound, model.lowerBound().fastLowerBound(o2.state));
            int comp = Double.compare(v1, v2);
            return comp == 0 ? model.ranking().compare(o1.state, o2.state) : comp;
        });

        // The best nodes are at the beginning (index 0). We keep the first `maxWidth` nodes.
        // We drop the remaining nodes from `maxWidth` to `size`.
        List<Node<T>> toDrop = new ArrayList<>(layerNodes.subList(maxWidth, layerNodes.size()));
        for (Node<T> n : toDrop) {
            stateMap.remove(n.state);
            for (Edge<T> e : n.inEdges) {
                e.origin.outEdges.remove(e);
            }
        }

        layerNodes.removeAll(toDrop);
    }

    private void computeBestBackward() {
        // Topological sort backwards from Target
        // We can just use a priority queue based on layer (highest layer first)
        PriorityQueue<Node<T>> pq = new PriorityQueue<>((a, b) -> Integer.compare(b.layer, a.layer));
        Set<Node<T>> visited = new HashSet<>();

        targetNode.backwardBound = 0.0;
        pq.add(targetNode);
        visited.add(targetNode);

        while (!pq.isEmpty()) {
            Node<T> n = pq.poll();

            double cur = n.backwardBound;

            for (Edge<T> e : n.inEdges) {
                Node<T> p = e.origin;
                double localBound = model.relaxation().localCost(p.state, e.label, n.state);
                double ep = saturatedAdd(cur, saturatedAdd(e.cost, localBound));

                if (isBetter(ep, p.backwardBound) || p.backwardBound == Double.POSITIVE_INFINITY) {
                    p.backwardBound = ep;
                }

                if (!visited.contains(p)) {
                    visited.add(p);
                    pq.add(p);
                }
            }
        }
    }

    private void updateCache() {
        if (!cache.isPresent()) return;
        for (Node<T> n : stateMap.values()) {
            if (n.backwardBound != Double.POSITIVE_INFINITY) {
                double thresholdValue = saturatedDiff(primalBound, n.backwardBound);
                Threshold t = new Threshold(thresholdValue, n.isExact);
                cache.get().updateThreshold(n.state, n.layer, t);
            }
        }
    }

    @Override
    public boolean isExact() {
        return exact;
    }

    @Override
    public Optional<Double> bestValue() {
        if (targetNode == null) return Optional.empty();
        return Optional.of(targetNode.bound);
    }

    @Override
    public Optional<Set<Decision>> bestSolution() {
        if (targetNode == null) return Optional.empty();
        Set<Decision> sol = new HashSet<>(rootSubProblem.getPath());
        Edge<T> edge = targetNode.bestParentEdge;
        int depth = targetNode.layer;
        while (edge != null) {
            depth--;
            sol.add(new Decision(depth, edge.label)); // Layer variable is mocked in nolayer
            edge = edge.origin.bestParentEdge;
        }
        return Optional.of(sol);
    }

    @Override
    public Iterator<SubProblem<T>> exactCutset() {
        List<SubProblem<T>> cutset = new ArrayList<>();
        // Nodes that are exact, but have at least one inexact child, or couldn't be expanded fully
        // This logic requires marking nodes properly.
        // For simplicity, a standard cutset is all exact nodes whose children were pruned/merged.
        // Or we can just return exact nodes that were not fully expanded.
        for (Node<T> n : stateMap.values()) {
            if (n.isExact && !n.isSink(problem)) {
                boolean allKidsExact = true;
                for (Edge<T> e : n.outEdges) {
                    if (!e.destination.isExact) {
                        allKidsExact = false;
                        break;
                    }
                }
                if (!allKidsExact || n.outEdges.isEmpty()) {
                    cutset.add(new SubProblem<>(n.state, n.bound, saturatedAdd(n.bound, n.backwardBound), pathOfNode(n)));
                }
            }
        }
        return cutset.iterator();
    }

    @Override
    public boolean relaxedBestPathIsExact() {
        if (targetNode == null) return false;
        if (!targetNode.isExact) return false;
        Edge<T> edge = targetNode.bestParentEdge;
        while (edge != null) {
            if (!edge.origin.isExact) return false;
            edge = edge.origin.bestParentEdge;
        }

        if (targetNode.bound < -1477.0) {
            System.out.println("FOUND INVALID EXACT PATH WITH BOUND " + targetNode.bound);
            edge = targetNode.bestParentEdge;
            while (edge != null) {
                System.out.println("Path node: " + edge.origin.state + " exact: " + edge.origin.isExact);
                edge = edge.origin.bestParentEdge;
            }
        }

        return true;
    }

    private Set<Decision> pathOfNode(Node<T> n) {
        Set<Decision> path = new HashSet<>(rootSubProblem.getPath());
        Edge<T> e = n.bestParentEdge;
        int depth = n.layer;
        while (e != null) {
            depth--;
            path.add(new Decision(depth, e.label));
            e = e.origin.bestParentEdge;
        }
        return path;
    }

    @Override
    public String exportAsDot() {
        return "digraph MDD {}";
    }

    @Override
    public int nbNodes() {
        return stateMap.size();
    }

    @Override
    public double minLowerBound() {
        return rootNode.bound;
    }

    public static class Node<T> {
        public final T state;
        public final List<Edge<T>> outEdges = new ArrayList<>();
        public final List<Edge<T>> inEdges = new ArrayList<>();
        public int layer;
        public double bound;
        public double backwardBound;
        public boolean isExact = true;
        public Edge<T> bestParentEdge = null;

        public Node(T state) {
            this.state = state;
            this.bound = Double.POSITIVE_INFINITY;
            this.backwardBound = Double.POSITIVE_INFINITY;
        }

        public boolean isSink(Problem<T> problem) {
            return problem.isTarget(state);
        }
    }

    public static class Edge<T> {
        public final int label;
        public final double cost;
        public Node<T> origin;
        public Node<T> destination;

        public Edge(Node<T> origin, Node<T> destination, int label, double cost) {
            this.origin = origin;
            this.destination = destination;
            this.label = label;
            this.cost = cost;
        }
    }
}
