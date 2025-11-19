package org.ddolib.ddo.core.heuristics.cluster;

import org.ddolib.ddo.core.mdd.NodeSubProblem;
import org.ddolib.modeling.StateRanking;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * This strategy select the nodes based on the objective value of the best path leading to them.
 * It requires a problem-specific StateRanking comparator to break the ties between nodes of same cost.
 * @param <T>
 */
public class CostBased<T> implements ReductionStrategy<T> {

    final private NodeSubroblemComparator<T> ranking;

    public CostBased(final StateRanking<T> ranking) {
        this.ranking = new NodeSubroblemComparator<>(ranking);
    }

    @Override
    public void setSeed(long seed) {
        return;
    }

    /**
     * Select the layer.size() - maxWidth - 1 nodes with the worst cost on the layer.
     * Add the end the maxWidth - 1 unselected nodes are still in the layer
     * @param layer the layer
     * @param maxWidth the desired maximal width after the restriction and relaxation
     * @return
     */
    public List<NodeSubProblem<T>>[] defineClusters(List<NodeSubProblem<T>> layer, int maxWidth) {
        layer.sort(ranking);

        List<NodeSubProblem<T>>[] cluster = new List[1];
        cluster[0] = new ArrayList<>(layer.subList(maxWidth-1, layer.size()));
        layer.subList(maxWidth-1, layer.size()).clear();

        return cluster;
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
            double cmp = o1.getValue() - o2.getValue();
            if (cmp == 0 && delegate != null) {
                return delegate.compare(o1.state, o2.state);
            } else {
                return Double.compare(o1.getValue(), o2.getValue());
            }
        }
    }
}
