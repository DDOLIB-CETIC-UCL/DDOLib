package org.ddolib.ddo.core.heuristics.cluster;

import org.ddolib.ddo.core.mdd.NodeSubProblem;

import java.util.*;

/**
 * This strategy uses partitioning from monotonous bisector trees to create the clusters.
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

        Map<T, Double> distanceWithPivot = new HashMap<>();
        PriorityQueue<ClusterNode> pqClusters = new PriorityQueue<>(Comparator.reverseOrder());

        // Selection of the two first pivots
        Collections.shuffle(layer, rnd);
        NodeSubProblem<T> pivotA = layer.getFirst();
        NodeSubProblem<T> pivotB = selectFarthest(pivotA, layer);
        for (int i = 0; i < 5; i++) {
            NodeSubProblem<T> newPivotA = selectFarthest(pivotB, layer);
            if (pivotA == newPivotA) {
                break;
            } else {
                pivotA = newPivotA;
                pivotB = selectFarthest(pivotA, layer);
            }
        }

         // Computes and store the distance between each element and the first pivot
        for (NodeSubProblem<T> node: layer) {
            distanceWithPivot.put(node.state, distance.distance(node.state, pivotA.state));
        }

        pqClusters.add(new ClusterNode(0.0 ,new ArrayList<>(layer), pivotA, pivotB));

        while (pqClusters.size() < maxWidth) {

            // Poll the next cluster two divide
            ClusterNode nodeCurrent = pqClusters.poll();
            assert nodeCurrent != null;
            List<NodeSubProblem<T>> current = nodeCurrent.cluster;

            pivotA = nodeCurrent.pivot;
            pivotB = nodeCurrent.mostDistant;

            // Generates the two clusters
            List<NodeSubProblem<T>> newClusterA = new ArrayList<>(current.size());
            List<NodeSubProblem<T>> newClusterB = new ArrayList<>(current.size());

            newClusterA.add(pivotA);
            newClusterB.add(pivotB);

            double maxDistA = 0;
            double maxDistB = 0;
            NodeSubProblem<T> mostDistantA = null;
            NodeSubProblem<T> mostDistantB = null;

            for (NodeSubProblem<T> node : current) {
                if (node.state.equals(pivotA.state) || node.state.equals(pivotB.state)) {
                    continue;
                }

                double distWithA = distanceWithPivot.get(node.state);
                double distWithB = distance.distance(node.state, pivotB.state);

                if (distWithA < distWithB) {
                    if (distWithA > maxDistA) {
                        maxDistA = distWithA;
                        mostDistantA = node;
                    }
                    newClusterA.add(node);
                } else {
                    if (distWithB > maxDistB) {
                        maxDistB = distWithB;
                        mostDistantB = node;
                    }
                    newClusterB.add(node);
                    distanceWithPivot.put(node.state, distWithB);
                }
            }

            assert !newClusterA.isEmpty();
            assert !newClusterB.isEmpty();

            // Add the two clusters to the queue
            pqClusters.add(new ClusterNode(maxDistA, newClusterA, pivotA, mostDistantA));
            pqClusters.add(new ClusterNode(maxDistB, newClusterB, pivotB, mostDistantB));
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
    private NodeSubProblem<T> selectFarthest(NodeSubProblem<T> ref, List<NodeSubProblem<T>> nodes) {
        double maxDistance = -1;
        NodeSubProblem<T> farthest = null;
        for (NodeSubProblem<T> node : nodes) {
            double currentDistance = distance.distance(node.state, ref.state);
            if (currentDistance > maxDistance && !node.state.equals(ref.state)) {
                maxDistance = currentDistance;
                farthest = node;
            }
        }
        return farthest;
    }

    private class ClusterNode implements Comparable<ClusterNode> {
        final double priority; // the maximal distance between the pivot and one of the other node in the cluster
        final NodeSubProblem<T> pivot; // the pivot associated to this cluster
        final NodeSubProblem<T> mostDistant; // If the cluster is split, this node will be the pivot of the new cluster
        final List<NodeSubProblem<T>> cluster;

        public ClusterNode(double priority,
                           List<NodeSubProblem<T>> cluster,
                           NodeSubProblem<T> pivot,
                           NodeSubProblem<T> mostDistant) {
            this.priority = priority;
            this.cluster = cluster;
            this.pivot = pivot;
            this.mostDistant = mostDistant;
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
