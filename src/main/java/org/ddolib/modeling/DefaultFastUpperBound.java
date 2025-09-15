package org.ddolib.modeling;

import java.util.Set;

/**
 * Default implementation of the fast upper bound that always returns {@code Double.POSITIVE_INFINITY}.
 *
 * @param <T> The type of the states.
 */
public class DefaultFastUpperBound<T> implements FastUpperBound<T> {
    @Override
    public double fastUpperBound(T state, Set<Integer> variables) {
        return 1000;
    }
}
