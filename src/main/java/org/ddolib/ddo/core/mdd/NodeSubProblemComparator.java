package org.ddolib.ddo.core.mdd;

import org.ddolib.modeling.StateRanking;

import java.util.Comparator;

/**
 * This utility class implements a decorator pattern to sort NodeSubProblems by their value then state
 */
public final class NodeSubProblemComparator<T> implements Comparator<NodeSubProblem<T>> {
    /**
     * This is the decorated ranking
     */
    private final StateRanking<T> delegate;

    /**
     * Creates a new instance
     *
     * @param delegate the decorated ranking
     */
    public NodeSubProblemComparator(final StateRanking<T> delegate) {
        this.delegate = delegate;
    }

    @Override
    public int compare(NodeSubProblem<T> o1, NodeSubProblem<T> o2) {
        double cmp = o1.node.value - o2.node.value;
        if (cmp == 0) {
            return delegate.compare(o1.state, o2.state);
        } else {
            return Double.compare(o1.node.value, o2.node.value);
        }
    }
}