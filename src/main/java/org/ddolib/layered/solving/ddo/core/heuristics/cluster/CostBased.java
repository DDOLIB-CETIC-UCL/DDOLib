package org.ddolib.layered.solving.ddo.core.heuristics.cluster;

import org.ddolib.layered.modeling.StateRanking;
import org.ddolib.layered.solving.ddo.core.mdd.NodeSubProblem;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * This strategy select the nodes based on the objective value of the best path leading to them.
 * It requires a problem-specific StateRanking comparator to break the ties between nodes of same cost.
 *
 * @param <T>
 */
public class CostBased<T> implements ReductionStrategy<T> {

    final private NodeSubroblemComparator<T> ranking;

    public CostBased(final StateRanking<T> ranking) {
        this.ranking = new NodeSubroblemComparator<>(ranking);
    }

    /**
     * Select the layer.size() - maxWidth - 1 nodes with the worst cost on the layer.
     * Add the end the maxWidth - 1 unselected nodes are still in the layer
     *
     * @param layer    the layer
     * @param maxWidth the desired maximal width after the restriction and relaxation
     * @return
     */
    public List<NodeSubProblem<T>>[] defineClusters(List<NodeSubProblem<T>> layer, int maxWidth) {
        layer.sort(ranking);
        int nbClusters = Math.min(layer.size(), maxWidth);
        List<NodeSubProblem<T>>[] cluster = new List[nbClusters];
        // take the best min(maxWidth, layerSize) - 1 nodes as individual clusters
        for (int i = 0; i < nbClusters - 1; i++) {
            cluster[i] = new ArrayList<>(1);
            cluster[i].add(layer.get(i));
        }
        // put the rest in the last cluster
        cluster[nbClusters - 1] = new ArrayList<>(layer.size() - nbClusters + 1);
        for (int i = nbClusters - 1; i < layer.size(); i++) {
            cluster[nbClusters - 1].add(layer.get(i));
        }
        return cluster;
    }

    /**
     * This utility class implements a decorator pattern to sort NodeSubProblems by their value then state
     *
     * @param delegate This is the decorated ranking
     */
    private record NodeSubroblemComparator<T>(StateRanking<T> delegate) implements Comparator<NodeSubProblem<T>> {
        /**
         * Creates a new instance
         *
         * @param delegate the decorated ranking
         */
        private NodeSubroblemComparator {
        }

        @Override
        public int compare(NodeSubProblem<T> o1, NodeSubProblem<T> o2) {
            int cmp = Double.compare(o1.node.value, o2.node.value);
            if (cmp == 0 && delegate != null) {
                return delegate.compare(o1.state, o2.state);
            } else {
                return cmp;
            }
        }
    }
}
