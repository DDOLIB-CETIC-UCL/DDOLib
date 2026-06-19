package org.ddolib.modeling.nolayer;

import java.util.Collection;

/**
 * Defines the relaxation rules for merging states in unlayered decision diagrams.
 *
 * @param <T> the type representing the states
 */
public interface Relaxation<T> {

    /**
     * Merges a collection of states into a single relaxed state.
     *
     * @param states the collection of states to merge
     * @return a new relaxed state representing the merged states
     */
    T merge(Collection<T> states);

    /**
     * Optional method to compute a local bound (or backward bound component)
     * when taking a transition from an origin to a destination state.
     * This bound helps in providing a stronger dual bound in the backward pass.
     * By default, it returns 0.0.
     *
     * @param origin      the state before the transition
     * @param label       the transition action/label
     * @param destination the state after the transition
     * @return the local bound cost
     */
    default double localCost(T origin, int label, T destination) {
        return 0.0;
    }
}
