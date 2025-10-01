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
public class GHPAlt<T> implements ReductionStrategy<T> {

    final private StateDistance<T> distance;
    final private Relaxation<T> relaxation;
    final private Random rnd;
    final private T rootState;
    private boolean mostDistantPivot;

    public GHPAlt(StateDistance<T> distance, Relaxation<T> relaxation, T rootState) {
        this.distance = distance;
        rnd = new Random();
        mostDistantPivot = true;
        this.relaxation = relaxation;
        this.rootState = rootState;
    }

    /**
     * Defines if the two selected pivot for partitioning must be the most distant in the cluster.
     * Default is true.
     * @param mostDistantPivot
     */
    public void setMostDistantPivot(boolean mostDistantPivot) {
        this.mostDistantPivot = mostDistantPivot;
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

        PriorityQueue<ClusterNode> pqClusters = new PriorityQueue<>();
        pqClusters.add(new ClusterNode(0.0 ,new ArrayList<>(layer)));

        while (pqClusters.size() < maxWidth) {

            // Poll the next cluster to divide
            ClusterNode nodeCurrent = pqClusters.poll();
            assert nodeCurrent != null;
            List<NodeSubProblem<T>> current = nodeCurrent.cluster;

            // Selection of the two pivot
            Collections.shuffle(current, rnd);
            NodeSubProblem<T> pivotA = current.getFirst();
            assert pivotA != null;
            NodeSubProblem<T> pivotB;
            if (!mostDistantPivot) {
                pivotB = current.get(1);
            } else {
                pivotB = selectFarthest(pivotA, current);
                assert pivotB != null;
                for (int i = 0; i < 5; i++) {
                    pivotA = selectFarthest(pivotB, current);
                    pivotB = selectFarthest(pivotA, current);
                }
            }


            // Generates the two clusters
            List<NodeSubProblem<T>> newClusterA = new ArrayList<>(current.size());
            List<NodeSubProblem<T>> newClusterB = new ArrayList<>(current.size());

            newClusterA.add(pivotA);
            newClusterB.add(pivotB);

            double maxDistA = 0;
            double maxDistB = 0;

            for (NodeSubProblem<T> node : current) {
                if (node.state.equals(pivotA.state) || node.state.equals(pivotB.state)) {
                    continue;
                }

                double distWithA = distance.distance(node.state, pivotA.state);
                double distWithB = distance.distance(node.state, pivotB.state);

                if (distWithA < distWithB) {
                    newClusterA.add(node);
                } else {
                    newClusterB.add(node);
                }
            }

            T mergedA = relaxation.mergeStates(new NodeSubProblemsAsStateIterator<>(newClusterA.iterator()));
            T mergedB = relaxation.mergeStates(new NodeSubProblemsAsStateIterator<>(newClusterB.iterator()));
            double priorityA = newClusterA.size() == 1 ? Double.MAX_VALUE : distance.distance(mergedA, rootState);
            double priorityB = newClusterB.size() == 1 ? Double.MAX_VALUE : distance.distance(mergedB, rootState);

            // Add the two clusters to the queue
            pqClusters.add(new ClusterNode(priorityA, newClusterA));
            pqClusters.add(new ClusterNode(priorityB, newClusterB));
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
        final double priority;
        final List<NodeSubProblem<T>> cluster;

        public ClusterNode(double maxDistance, List<NodeSubProblem<T>> cluster) {
            this.priority = maxDistance;
            this.cluster = cluster;
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
