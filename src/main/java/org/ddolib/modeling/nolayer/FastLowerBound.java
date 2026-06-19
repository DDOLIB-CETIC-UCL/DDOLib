package org.ddolib.modeling.nolayer;

/**
 * Interface defining a lower bounding function for a state.
 *
 * @param <T> the type representing the state
 */
public interface FastLowerBound<T> {
    /**
     * Computes a lower bound on the remaining cost from the given state
     * to reach any valid target state.
     *
     * @param state the current state
     * @return a lower bound on the remaining cost
     */
    double fastLowerBound(T state);
}
