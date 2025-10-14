package org.ddolib.ddo.core.heuristics.cluster;

import org.ddolib.ddo.core.mdd.NodeSubProblem;
import org.ddolib.ddo.core.mdd.NodeSubProblemsAsStateIterator;
import org.ddolib.modeling.Relaxation;

import java.util.*;

/**
 * This strategy uses Generalized Hyperplan partitioning to create the clusters.
 * It requires a problem-specific StateDistance function that computes the distance between two states
 * @param <T> the type of state
 */
public class MBP<T> implements ReductionStrategy<T> {

    final private StateDistance<T> distance;
    final private Random rnd;

    public MBP(StateDistance<T> distance) {
        this.distance = distance;
        rnd = new Random();
    }

    /**
     * @param seed
     */
    public void setSeed(int seed) {
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
        for (NodeSubProblem<T> node: layer) {
            distanceWithPivot.put(node.state, distance.distance(pivotA.state, node.state));
        }

        PriorityQueue<ClusterNode> pqClusters = new PriorityQueue<>(Comparator.reverseOrder());
        pqClusters.add(new ClusterNode(0.0 ,new ArrayList<>(layer), pivotA, pivotB));

        while (pqClusters.size() < maxWidth) {

            // Poll the next cluster to divide
            ClusterNode nodeCurrent = pqClusters.poll();
            assert nodeCurrent != null;
            List<NodeSubProblem<T>> current = nodeCurrent.cluster;
            pivotA = nodeCurrent.pivot;
            pivotB = nodeCurrent.furthestFromPivot;

            // Generates the two clusters
            List<NodeSubProblem<T>> newClusterA = new ArrayList<>(current.size());
            List<NodeSubProblem<T>> newClusterB = new ArrayList<>(current.size());

            NodeSubProblem<T> furthestFromA = pivotA;
            double maxDistanceA = 0.0;
            NodeSubProblem<T> furthestFromB = pivotB;
            double maxDistanceB = 0.0;

            newClusterA.add(pivotA);
            newClusterB.add(pivotB);
            distanceWithPivot.put(pivotB.state, 0.0);

            for (NodeSubProblem<T> node : current) {
                if (node.state.equals(pivotA.state) || node.state.equals(pivotB.state)) {
                    continue;
                }

                double distWithA = distanceWithPivot.get(node.state);
                double distWithB = distance.distance(node.state, pivotB.state);

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

            double priorityA = newClusterA.size() == 1 ? 0.0 : maxDistanceA;
            double priorityB = newClusterB.size() == 1 ? 0.0 : maxDistanceB;

            // Add the two clusters to the queue
            pqClusters.add(new ClusterNode(priorityA, newClusterA, pivotA, furthestFromA));
            pqClusters.add(new ClusterNode(priorityB, newClusterB, pivotB, furthestFromB));
        }

        // Retrieve the clusters from the queue
        List<NodeSubProblem<T>>[] clusters = new List[pqClusters.size()];
        int index = 0;
        for (ClusterNode cluster : pqClusters) {
            clusters[index] = cluster.cluster;
            index++;
        }
        layer.clear();
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
            double currentDistance = distance.distance(node.state, ref.state);
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

        public ClusterNode(double priority, List<NodeSubProblem<T>> cluster, NodeSubProblem<T> pivot, NodeSubProblem<T> furthestFromPivot) {
            this.priority = priority;
            this.cluster = cluster;
            this.pivot = pivot;
            this.furthestFromPivot = furthestFromPivot;
        }

        @Override
        public int compareTo(ClusterNode o) {
            if (this.priority == o.priority) {
                return Integer.compare(this.cluster.size(), o.cluster.size());
            } else {
                return Double.compare(this.priority, o.priority);
            }
        }
    }

}
