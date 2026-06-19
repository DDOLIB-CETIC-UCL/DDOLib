package org.ddolib.modeling.nolayer;

/**
 * Default implementation of {@link FastLowerBound} that always returns 0.0.
 * @param <T> the type representing the state
 */
public class DefaultFastLowerBound<T> implements FastLowerBound<T> {
    @Override
    public double fastLowerBound(T state) {
        return 0.0;
    }
}
