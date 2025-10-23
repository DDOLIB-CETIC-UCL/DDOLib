package org.ddolib.modeling;
/**
 * Defines a dominance relation used to compare and prune states
 * during the exploration of decision diagrams or search spaces.
 * <p>
 * A {@code Dominance} relation provides a way to determine whether one
 * state of the problem is at least as good as (i.e., dominates) another,
 * allowing the solver to discard redundant or inferior subproblems.
 * </p>
 *
 * <p>
 * The dominance check is an essential optimization mechanism in decision diagram–based
 * solvers, as it helps reduce the search space by recognizing equivalent or
 * dominated states that do not need to be further expanded.
 * </p>
 *
 * @param <T> the type representing the problem state
 */
public interface Dominance<T> {
    /**
     * Returns a canonical key associated with a given state.
     * <p>
     * This key is typically used to identify equivalent states or to group
     * states that share the same dominance characteristics. Implementations
     * should ensure that two states with identical keys are comparable under
     * the same dominance criteria.
     * </p>
     *
     * @param state the state for which a dominance key is requested
     * @return an object uniquely (or canonically) representing the given state
     */
    Object getKey(T state);
    /**
     * Tests whether {@code state1} is dominated by or equivalent to {@code state2}.
     * <p>
     * A state {@code state1} is said to be dominated by {@code state2} if every
     * feasible continuation from {@code state1} cannot yield a better objective
     * value than one from {@code state2}. In other words, {@code state2}
     * is at least as good as {@code state1} in all relevant aspects of the problem.
     * </p>
     *
     * @param state1 the first state to compare
     * @param state2 the second state to compare against
     * @return {@code true} if {@code state1} is dominated by or equivalent to {@code state2};
     *         {@code false} otherwise
     */

    boolean isDominatedOrEqual(T state1, T state2);
}
