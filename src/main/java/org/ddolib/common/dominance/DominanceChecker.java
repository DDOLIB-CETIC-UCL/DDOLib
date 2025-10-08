package org.ddolib.common.dominance;

import org.ddolib.modeling.Dominance;

/**
 * Object that, given a dominance, will check if a state is dominated.
 *
 * @param <T> The type of states.
 * @param <K> The type of dominance keys.
 */
public abstract class DominanceChecker<T, K> {

    /**
     * Problem specific dominance rule.
     */
    protected final Dominance<T, K> dominance;

    /**
     * Instantiate a new Dominance checker.
     *
     * @param dominance The problem specific dominance rule used by the checker.
     */
    protected DominanceChecker(Dominance<T, K> dominance) {
        this.dominance = dominance;
    }

    /**
     * Checks whether the input state is dominated and updates the front of non-dominated nodes.
     *
     * @param state    The state on which test dominance.
     * @param depth    The depth of the state in the MDD.
     * @param objValue The length of the longest path from the root to the input state.
     */
    public abstract void updateDominance(T state, int depth, double objValue);

    /**
     * Returns whether the input state is dominated
     *
     * @param state    The state on which test dominance.
     * @param depth    The depth of the state in the MDD.
     * @param objValue The length of the longest path from the root to the input state.
     * @return Whether {@code state} is dominated by another state at the same depth.
     */
    public abstract boolean isDominated(T state, int depth, double objValue);

    /**
     * Returns a new checker, clearing the internal states.
     *
     * @return An all new checker
     */
    public abstract DominanceChecker<T, K> clear();
}
