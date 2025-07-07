package org.ddolib.ddo.implem.heuristics;

import org.ddolib.ddo.heuristics.FastUpperBoundHeuristic;

import java.util.Set;

/**
 * Default implementation of the fast upper bound that always returns {@code Double.MAX_VALUE}.
 *
 * @param <T> The type of the states.
 */
public class DefaultFastUpperBound<T> implements FastUpperBoundHeuristic<T> {
    @Override
    public double fastUpperBound(T state, Set<Integer> variables) {
        return Double.MAX_VALUE;
    }
}
