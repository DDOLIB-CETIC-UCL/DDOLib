package org.ddolib.nolayer.modeling;

/**
 * Default implementation of {@link FastLowerBound} that always returns 0.0.
 *
 * @param <T> the type representing the state
 */
public class DefaultFastLowerBound<T> implements FastLowerBound<T> {

    /**
     * Creates a new instance of this default fast lower bound.
     */
    public DefaultFastLowerBound() {
    }

    @Override
    public double fastLowerBound(T state) {
        return 0.0;
    }
}
