package org.ddolib.modeling;

/**
 * Define problem specific dominance rules.
 *
 * @param <T> The type of the states.
 * @param <K> The type of dominance keys.
 */
public interface Dominance<T, K> {


    /**
     * Returns the key associated to the input state. Only states with the same key are compared.
     *
     * @param state The input state.
     * @return The dominance key associated to the input state
     */
    K getKey(T state);

    /**
     * Returns whether the first state is dominated or equal to the second one.
     *
     * @param state1 The first state to compare.
     * @param state2 The second state to compare.
     * @return Whether  {@code state1} is dominated by {@code state2} or if {@code state1 ==
     * state2}.
     */
    boolean isDominatedOrEqual(T state1, T state2);
}
