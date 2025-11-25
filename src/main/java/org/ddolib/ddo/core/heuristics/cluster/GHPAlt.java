package org.ddolib.ddo.core.heuristics.cluster;

import org.ddolib.ddo.core.mdd.NodeSubProblem;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Relaxation;

import java.util.*;

/**
 * This strategy uses Generalized Hyperplan partitioning to create the clusters.
 * It requires a problem-specific StateDistance function that computes the distance between two states
 * @param <T> the type of state
 */
public class GHPAlt<T> implements ReductionStrategy<T> {

    final private StateDistance<T> distance;
    final private Random rnd;
    final private Relaxation<T> relaxation;
    final private Problem<T> instance;

    public GHPAlt(StateDistance<T> distance, Relaxation<T> relaxation, Problem<T> instance) {
        this.distance = distance;
        this.relaxation = relaxation;
        this.instance = instance;
        rnd = new Random();
    }

    public void setSeed(long seed) {
        this.rnd.setSeed(seed);
    }

    /**
     * Computes maxWidth clusters using Generalized Hyperplan Partitioning.
     * Add the end all the nodes in the layer are added to a cluster, and the layer is empty.
     * @param layer the layer
     * @param maxWidth the desired maximal width after the restriction and relaxation
     * @return an array of List representing the clusters.
     */
    @Override
    public List<NodeSubProblem<T>>[] defineClusters(List<NodeSubProblem<T>> layer, int maxWidth) {

        Map<T, Double> distanceWithPivot = new HashMap<>(layer.size());

        Collections.shuffle(layer, rnd);
        NodeSubProblem<T> pivotA = layer.getFirst();
        NodeSubProblem<T> pivotB = selectFurthest(pivotA, layer);
        pivotA = selectFurthest(pivotB, layer);
        pivotB = selectFurthest(pivotA, layer);
        pivotA = selectFurthest(pivotB, layer);
        pivotB = selectFurthest(pivotA, layer);
        // System.out.printf("%s, %s, %f %n", pivotA, pivotB, distance.distance(pivotA.state, pivotB.state));
        for (NodeSubProblem<T> node: layer) {
            distanceWithPivot.put(node.state, distance.distance(pivotA.state, node.state));
        }

        T result = relaxation.mergeStates(layer.stream().map(x-> x.state).iterator());
        Set<T> resultingLayer = new HashSet<>();
        resultingLayer.add(result);

        PriorityQueue<ClusterNode> pqClusters = new PriorityQueue<>(Comparator.reverseOrder());
        pqClusters.add(new ClusterNode(0.0 ,new ArrayList<>(layer), pivotA, pivotB, result));

        while (resultingLayer.size() < maxWidth) {
            // System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");

            // Poll the next cluster to divide
            // System.out.println(pqClusters);
            ClusterNode nodeCurrent = pqClusters.poll();
            // System.out.println(nodeCurrent);
            assert nodeCurrent != null;
            List<NodeSubProblem<T>> current = nodeCurrent.cluster;
            pivotA = nodeCurrent.pivot;
            pivotB = nodeCurrent.furthestFromPivot;
            // System.out.println(pivotA);
            // System.out.println(pivotB);

            // Generates the two clusters
            List<NodeSubProblem<T>> newClusterA = new ArrayList<>(current.size());
            List<NodeSubProblem<T>> newClusterB = new ArrayList<>(current.size());

            NodeSubProblem<T> furthestFromA = pivotA;
            double maxDistanceA = -1;
            NodeSubProblem<T> furthestFromB = pivotB;
            double maxDistanceB = -1;

            newClusterA.add(pivotA);
            newClusterB.add(pivotB);
            distanceWithPivot.put(pivotB.state, 0.0);

            for (NodeSubProblem<T> node : current) {
                if (node.state.equals(pivotA.state) || node.state.equals(pivotB.state)) {
                    continue;
                }

                double distWithA = distanceWithPivot.get(node.state);
                // double distWithB = distance.distance(node.state, pivotB.state);
                double distWithB = distance.distance(node, pivotB);

                if (distWithA < distWithB) {
                    newClusterA.add(node);
                    if (distWithA > maxDistanceA) {
                        furthestFromA = node;
                        maxDistanceA = distWithA;
                    }
                } else {
                    newClusterB.add(node);
                    distanceWithPivot.put(node.state, distWithB);
                    if (distWithB > maxDistanceB) {
                        furthestFromB = node;
                        maxDistanceB = distWithB;
                    }
                }
            }
            // System.out.println(newClusterA);
            // System.out.println(newClusterB);

            T mergedA = relaxation.mergeStates(newClusterA.stream().map(x -> x.state).iterator());
            T mergedB = relaxation.mergeStates(newClusterB.stream().map(x -> x.state).iterator());

            double priorityA = newClusterA.size() == 1 ? -1 :
                    Math.max(distance.distance(pivotA.state, mergedA), distance.distance(furthestFromA.state, mergedA));
            double priorityB = newClusterB.size() == 1 ? -1 :
                    Math.max(distance.distance(pivotB.state, mergedB), distance.distance(furthestFromB.state, mergedB));

            // double priorityA = newClusterA.size() == 1 ? -1 : distance.distance(pivotA, furthestFromA);
            // double priorityB = newClusterB.size() == 1 ? -1 : distance.distance(pivotB, furthestFromB);

            // double priorityA = newClusterA.size() == 1 ? -1 : maxDistanceA;
            // double priorityB = newClusterB.size() == 1 ? -1 : maxDistanceB;

            // Add the two clusters to the queue
            pqClusters.add(new ClusterNode(priorityA, newClusterA, pivotA, furthestFromA, mergedA));
            pqClusters.add(new ClusterNode(priorityB, newClusterB, pivotB, furthestFromB, mergedB));

            resultingLayer.clear();
            for (ClusterNode node : pqClusters) {
                resultingLayer.add(node.result);
            }
        }

        // Retrieve the clusters from the queue
        List<NodeSubProblem<T>>[] clusters = new List[pqClusters.size()];
        int index = 0;
        // System.out.println("@@@@@@@@@@@@@@@@@@@");
        for (ClusterNode cluster : pqClusters) {
            // System.out.println(cluster.pivot);
            // System.out.println(cluster.furthestFromPivot);
            // System.out.println(cluster.cluster);
            clusters[index] = cluster.cluster;
            index++;
        }
        // System.out.println("@@@@@@@@@@@@@@@@@@@");
        /*System.out.println("@@@@@@@@@@@@@@@@@@@");
        for (List<NodeSubProblem<T>> cluster : clusters) {
            System.out.println(cluster);
        }
        System.out.println("@@@@@@@@@@@@@@@@@@@");*/
        return clusters;
    }

    /**
     * @param ref a node reference
     * @param nodes a cluster
     * @return the node in the given cluster that is the most distant of the given reference
     */
    private NodeSubProblem<T> selectFurthest(NodeSubProblem<T> ref, List<NodeSubProblem<T>> nodes) {
        double maxDistance = -1;
        NodeSubProblem<T> furthest = null;
        for (NodeSubProblem<T> node : nodes) {
            // double currentDistance = distance.distance(node.state, ref.state);
            double currentDistance = distance.distance(node, ref);
            if (currentDistance > maxDistance && !node.state.equals(ref.state)) {
                maxDistance = currentDistance;
                furthest = node;
            }
        }
        return furthest;
    }

    private class ClusterNode implements Comparable<ClusterNode> {
        final double priority;
        final List<NodeSubProblem<T>> cluster;
        final NodeSubProblem<T> pivot;
        final NodeSubProblem<T> furthestFromPivot;
        final T result;

        public ClusterNode(double priority,
                           List<NodeSubProblem<T>> cluster,
                           NodeSubProblem<T> pivot,
                           NodeSubProblem<T> furthestFromPivot,
                           T result) {
            this.priority = priority;
            this.cluster = cluster;
            this.pivot = pivot;
            this.furthestFromPivot = furthestFromPivot;
            this.result = result;
        }

        @Override
        public int compareTo(ClusterNode o) {
            if (this.priority == o.priority) {
                return Integer.compare(this.cluster.size(), o.cluster.size());
            } else {
                return Double.compare(this.priority, o.priority);
            }
        }

        @Override
        public String toString() {
            return this.cluster.toString();
        }
    }

}
