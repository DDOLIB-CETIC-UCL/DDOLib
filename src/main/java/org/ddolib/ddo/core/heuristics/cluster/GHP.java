package org.ddolib.ddo.core.heuristics.cluster;

import org.ddolib.ddo.core.mdd.NodeSubProblem;
import org.ddolib.ddo.core.mdd.NodeType;

import java.util.*;

/**
 * Generalized Hyperplane Partitioning (GHP) reduction strategy for decision diagram layers.
 *
 * <p>
 * This class implements {@link ReductionStrategy} and clusters nodes in a layer using
 * a distance-based partitioning method inspired by hyperplane separation. It requires
 * a problem-specific {@link StateDistance} function to compute distances between states.
 *
 * <p>
 * The GHP strategy works by:
 * <ol>
 *   <li>Selecting two distant pivot nodes from the layer</li>
 *   <li>Assigning each remaining node to the cluster of the closer pivot</li>
 *   <li>Recursively splitting clusters until the desired number of clusters ({@code maxWidth}) is reached</li>
 * </ol>
 *
 * <p>
 * A random number generator is used for tie-breaking and initial shuffling of the layer.
 *
 * @param <T> the type of states associated with the nodes
 */
public class GHP<T> implements ReductionStrategy<T> {
    /** Distance function used to measure distances between states. */
    final private StateDistance<T> distance;
    /** Random number generator for shuffling and tie-breaking. */
    final private Random rnd;


    /**
     * Constructs a GHP reduction strategy with a default random seed.
     *
     * @param distance the distance function used to compare states
     */
    public GHP(StateDistance<T> distance) {
        this.distance = distance;
        rnd = new Random();
    }
    /**
     * Constructs a GHP reduction strategy with a specified random seed.
     *
     * @param distance the distance function used to compare states
     * @param seed the random seed
     */
    public GHP(StateDistance<T> distance, long seed) {
        this.distance = distance;
        this.rnd = new Random(seed);
    }
    /**
     * Sets the seed of the internal random number generator.
     *
     * @param seed the new seed value
     */
    public void setSeed(long seed) {
        this.rnd.setSeed(seed);
    }

    /**
     * Partitions the given layer into clusters using Generalized Hyperplane Partitioning.
     *
     * <p>
     * The method recursively divides the layer by selecting pivot nodes and assigning
     * nodes to the cluster of the closest pivot. All nodes in the layer are included
     * in one of the resulting clusters, and the input layer is emptied.
     *
     * @param layer the list of nodes at the current layer
     * @param maxWidth the desired number of clusters (maximum width after reduction)
     * @return an array of clusters, each cluster being a list of nodes
     */
    @Override
    public List<NodeSubProblem<T>>[] defineClusters(List<NodeSubProblem<T>> layer, int maxWidth) {

        Map<T, Double> distanceWithPivot = new HashMap<>(layer.size());

        Collections.shuffle(layer, rnd);
        NodeSubProblem<T> pivotA = layer.getFirst();
        NodeSubProblem<T> pivotB = selectFurthest(pivotA, layer);
        pivotA = selectFurthest(pivotB, layer);
        pivotB = selectFurthest(pivotA, layer);
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

            double priorityA = newClusterA.size() == 1 ? -1 : maxDistanceA;
            double priorityB = newClusterB.size() == 1 ? -1 : maxDistanceB;

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

        return clusters;
    }

    /**
     * Selects the node in a cluster that is farthest from a reference node.
     *
     * @param ref the reference node
     * @param nodes the cluster of nodes to search
     * @return the node farthest from the reference
     */
    private NodeSubProblem<T> selectFurthest(NodeSubProblem<T> ref, List<NodeSubProblem<T>> nodes) {
        double maxDistance = -1;
        NodeSubProblem<T> furthest = null;
        for (NodeSubProblem<T> node : nodes) {
            double currentDistance = distance.distance(node.state, ref.state);
            // double currentDistance = distance.distance(node, ref);
            if (currentDistance > maxDistance && !node.state.equals(ref.state)) {
                maxDistance = currentDistance;
                furthest = node;
            }
        }
        return furthest;
    }
    /**
     * Internal class representing a cluster with its pivot nodes and priority.
     */
    private class ClusterNode implements Comparable<ClusterNode> {
        /** Priority of the cluster for splitting (based on maximum distance from pivot). */
        final double priority;
        /** List of nodes contained in the cluster. */
        final List<NodeSubProblem<T>> cluster;
        /** Primary pivot node of the cluster. */
        final NodeSubProblem<T> pivot;
        /** Node farthest from the primary pivot, used for recursive splitting. */
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

        @Override
        public String toString() {
            return this.cluster.toString();
        }
    }

}
