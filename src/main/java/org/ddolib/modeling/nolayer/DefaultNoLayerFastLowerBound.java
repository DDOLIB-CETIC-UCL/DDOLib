package org.ddolib.modeling.nolayer;

/**
 * Default implementation of {@link NoLayerFastLowerBound} that always returns 0.0.
 * @param <T> the type representing the state
 */
public class DefaultNoLayerFastLowerBound<T> implements NoLayerFastLowerBound<T> {
    @Override
    public double fastLowerBound(T state) {
        return 0.0;
    }
}
