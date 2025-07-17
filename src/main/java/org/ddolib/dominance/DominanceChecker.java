package org.ddolib.dominance;

import org.ddolib.modeling.Dominance;

/**
 * Object that, given a dominance, will check if a state is dominated.
 *
 * @param <T> The type of states.
 * @param <K> The type of dominance keys.
 */
public abstract class DominanceChecker<T, K> {

    protected final Dominance<T, K> dominance;

    protected DominanceChecker(Dominance<T, K> dominance) {
        this.dominance = dominance;
    }

    /**
     * Checks whether the input state is dominated and updates the front of non-dominated nodes.
     *
     * @param state    The state on which test dominance.
     * @param depth    The depth of the state in the MDD.
     * @param objValue The length of the longest path from the root to the input state.
     * @return Whether the input state is dominated.
     */
    public abstract boolean updateDominance(T state, int depth, double objValue);
}
