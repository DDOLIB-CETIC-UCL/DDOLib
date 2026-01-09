package org.ddolib.modeling;

import java.util.Set;

/**
 * Default implementation of the {@link FastLowerBound} interface that always returns
 * {@link Integer#MIN_VALUE} as the lower bound estimate.
 * <p>
 * This implementation can be used as a placeholder or a fallback when no
 * meaningful fast lower bound heuristic is available for a given problem.
 * It effectively disables lower bound pruning since the returned value is
 * the smallest possible integer.
 * </p>
 *
 * <p>In practice, a fast lower bound is a lightweight estimation of the
 * minimal achievable objective value from a given state and set of remaining variables.
 * This default implementation deliberately returns the lowest possible value,
 * ensuring that no pruning occurs based on this bound.</p>
 *
 * @param <T> the type representing the problem state
 * @see FastLowerBound
 */
public class DefaultFastLowerBound<T> implements FastLowerBound<T> {
    /**
     * Computes a trivial fast lower bound for the given state and remaining variables.
     * <p>
     * This default implementation always returns {@link Integer#MIN_VALUE},
     * effectively indicating that no lower bound information is available.
     * </p>
     *
     * @param state the current state for which the lower bound is estimated
     * @param variables the set of remaining variable indices yet to be assigned
     * @return always {@code Integer.MIN_VALUE}
     */
    @Override
    public double fastLowerBound(T state, Set<Integer> variables) {
        // must be very careful with this default implementation,
        // it must be a lower-bound, but when reaching the terminal state
        // with no remaining variables, it should return 0 otherwise
        // A* search will not work correctly as it will incorrectly terminate
        // by thinking it has found an optimal solution when poping the terminal state
        // from the open list.
        if (variables.isEmpty()) { return 0;}
        else return Integer.MIN_VALUE;
    }
}
