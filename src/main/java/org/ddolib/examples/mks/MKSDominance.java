package org.ddolib.examples.mks;

import org.ddolib.modeling.Dominance;
/**
 * Implements a dominance relation for Multi-dimensional Knapsack (MKS) states.
 *
 * <p>
 * In this dominance relation, one state {@code state1} is considered dominated by or equal to
 * another state {@code state2} if, in every dimension, {@code state1} has less than or equal
 * remaining capacity compared to {@code state2}.
 *
 * <p>
 * This is useful for pruning the search space in decision diagram optimization:
 * dominated states can be safely discarded without losing optimality.
 */
public class MKSDominance implements Dominance<MKSState> {
    /**
     * Returns a key for grouping states in dominance checks.
     *
     * <p>
     * Here, all states share the same key (0), meaning that all states are comparable
     * for dominance against each other.
     *
     * @param state the state
     * @return the key for dominance grouping (always 0)
     */
    @Override
    public Integer getKey(MKSState state) {
        return 0;
    }
    /**
     * Determines whether {@code state1} is dominated by or equal to {@code state2}.
     *
     * <p>
     * {@code state1} is dominated if its remaining capacity in every dimension is less than or
     * equal to the corresponding capacity in {@code state2}.
     *
     * @param state1 the first state to compare
     * @param state2 the second state to compare
     * @return {@code true} if {@code state1} is dominated by or equal to {@code state2}, {@code false} otherwise
     */
    @Override
    public boolean isDominatedOrEqual(MKSState state1, MKSState state2) {
        for (int dim = 0; dim < state1.capacities.length; dim++) {
            if (state1.capacities[dim] > state2.capacities[dim]) {
                return false;
            }
        }

        return true;
    }
}
