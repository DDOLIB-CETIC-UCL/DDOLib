package org.ddolib.common.dominance;

/**
 * Defines a dominance checking mechanism to prune search states in NoLayer models.
 *
 * @param <T> the type of states
 */
public interface NoLayerDominanceChecker<T> {

    /**
     * Evaluates if a state can be pruned based on previously seen states.
     *
     * @param state the current state to check
     * @param value the objective value associated with reaching the state
     * @return {@code true} if the state is dominated and should be pruned; {@code false} otherwise
     */
    boolean updateDominance(T state, double value);

    /**
     * Clears the internally cached states used for dominance checking.
     * This is useful to reset the checker's state between solver iterations.
     */
    default void clear() {}
}
