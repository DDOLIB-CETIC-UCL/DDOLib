package org.ddolib.ddo.heuristics;

import java.util.Set;

/**
 * Heuristic defining a fast upper bound for states
 *
 * @param <T> The type of the states
 */
public interface FastUpperBoundHeuristic<T> {


    /**
     * Returns a very rough estimation (upper bound) of the optimal value that could be
     * reached if state were the initial state.
     *
     * @param state     The state for which the estimate is to be computed.
     * @param variables The set of unassigned variables.
     * @return A very rough estimation (upper bound) of the optimal value that could be
     * reached if state were the initial state.
     */
    double fastUpperBound(final T state, final Set<Integer> variables);
}
