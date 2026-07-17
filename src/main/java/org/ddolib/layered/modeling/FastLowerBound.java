package org.ddolib.layered.modeling;


import java.util.Set;

/**
 * Interface for the fast lower bound method
 * Heuristic defining a fast lower bound for states
 *
 * @param <T> the type of the state
 */
public interface FastLowerBound<T> {


    /**
     * Returns a very rough estimation (upper bound) of the optimal value that could be
     * reached if state were the initial state.
     *
     * @param state     the state for which the estimate is to be computed
     * @param variables the set of unassigned variables
     * @return A very rough estimation (upper bound) of the optimal value that could be
     * reached if state were the initial state.
     */
    double fastLowerBound(final T state, final Set<Integer> variables);
}
