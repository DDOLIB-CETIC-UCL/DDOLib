package org.ddolib.ddo.core.heuristics.cluster;

import org.ddolib.ddo.core.mdd.LinkedDecisionDiagram.NodeSubProblem;
import org.ddolib.modeling.StateRanking;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CostBased<T> implements ReduceStrategy<T>{

    private NodeSubroblemComparator<T> ranking;

    public CostBased(final StateRanking<T> ranking) {
        this.ranking = new NodeSubroblemComparator<>(ranking);
    }

    public List<NodeSubProblem<T>>[] defineClusters(List<NodeSubProblem<T>> layer, int maxWidth) {
        layer.sort(ranking.reversed());

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
