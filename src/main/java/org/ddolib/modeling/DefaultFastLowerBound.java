package org.ddolib.modeling;

import java.util.Set;

/**
 * Default implementation of the fast lower bound that always returns {@code Double.NEGATIVE_INFINITY}.
 *
 * @param <T> The type of the states.
 */
public class DefaultFastLowerBound<T> implements FastLowerBound<T> {
    @Override
    public double fastLowerBound(T state, Set<Integer> variables) {
        return Double.NEGATIVE_INFINITY;
    }
}
