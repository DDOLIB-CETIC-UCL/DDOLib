package org.ddolib.examples.tsptw;

import org.ddolib.modeling.Dominance;

/**
 * Dominance relation for the Traveling Salesman Problem with Time Windows (TSPTW).
 *
 * <p>
 * This class defines a dominance rule between two {@link TSPTWState} instances.
 * Two states are comparable if they share the same current position and the same
 * set of remaining locations to visit ({@code mustVisit}). Among such comparable states,
 * the state with the lower current time dominates the other.
 * </p>
 *
 * <p>
 * Dominance is used to prune the search space: if a state is dominated by another,
 * it can be safely discarded without losing optimality.
 * </p>
 */
public class TSPTWDominance implements Dominance<TSPTWState> {
    /**
     * Returns the dominance key for a given state, based on its current position
     * and the set of locations that still must be visited.
     *
     * @param state The state for which to compute the dominance key.
     * @return The {@link TSPTWDominanceKey} representing the key of this state.
     */
    @Override
    public TSPTWDominanceKey getKey(TSPTWState state) {
        return new TSPTWDominanceKey(state.position(), state.mustVisit());
    }

    /**
     * Checks whether {@code state1} is dominated by or equal to {@code state2}.
     *
     * <p>
     * {@code state1} is considered dominated or equal to {@code state2} if
     * it has the same position and mustVisit set, and its current time is
     * greater than or equal to that of {@code state2}.
     * </p>
     *
     * @param state1 The state being tested for dominance.
     * @param state2 The state to compare against.
     * @return {@code true} if {@code state1} is dominated by or equal to {@code state2}, {@code false} otherwise.
     */
    @Override
    public boolean isDominatedOrEqual(TSPTWState state1, TSPTWState state2) {
        return state1.time() >= state2.time();
    }
}

