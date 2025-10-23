package org.ddolib.ddo.core.mdd;

import org.ddolib.modeling.StateRanking;

import java.util.Comparator;

/**
 * Comparator for {@link NodeSubProblem} instances that sorts them first by their node value,
 * and then by the state using a provided {@link StateRanking} if the values are equal.
 * <p>
 * This class implements a decorator pattern, allowing a {@link StateRanking} to be used as a tie-breaker
 * when node values are identical. It is useful for prioritizing subproblems in search or decision diagram algorithms.
 * </p>
 *
 * @param <T> the type of state contained in the subproblems
 */
final class NodeSubProblemComparator<T> implements Comparator<NodeSubProblem<T>> {
    /** The decorated ranking used to break ties when node values are equal. */
    private final StateRanking<T> delegate;

    /**
     * Constructs a new comparator that uses the given ranking as a tie-breaker.
     *
     * @param delegate the {@link StateRanking} used to compare states when node values are equal
     */
    public NodeSubProblemComparator(final StateRanking<T> delegate) {
        this.delegate = delegate;
    }
    /**
     * Compares two {@link NodeSubProblem} instances.
     * <p>
     * First, compares by the node value. If the values are equal, the comparison is delegated
     * to the {@link StateRanking} in reversed order.
     * </p>
     *
     * @param o1 the first subproblem to compare
     * @param o2 the second subproblem to compare
     * @return a negative integer, zero, or a positive integer as the first subproblem
     *         is less than, equal to, or greater than the second
     */
    @Override
    public int compare(NodeSubProblem<T> o1, NodeSubProblem<T> o2) {
        double cmp = o1.node.value - o2.node.value;
        if (cmp == 0) {
            return delegate.reversed().compare(o1.state, o2.state);
        } else {
            return Double.compare(o1.node.value, o2.node.value);
        }
    }
}