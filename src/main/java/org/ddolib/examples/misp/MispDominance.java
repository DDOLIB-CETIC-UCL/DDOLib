package org.ddolib.examples.misp;

import org.ddolib.modeling.Dominance;

import java.util.BitSet;
/**
 * Implementation of a dominance relation for the Maximum Independent Set Problem (MISP).
 * <p>
 * In this context, one state {@code state1} is considered dominated by another state {@code state2}
 * if all vertices selected in {@code state1} are also selected in {@code state2}.
 * This allows pruning suboptimal states during the search.
 * </p>
 */
public class MispDominance implements Dominance<BitSet> {
    /**
     * Returns a key for the dominance relation.
     * <p>
     * This implementation always returns {@code 0} since no partitioning of states is needed
     * based on a specific key.
     * </p>
     *
     * @param state the current state
     * @return a key for the dominance relation
     */
    @Override
    public Integer getKey(BitSet state) {
        return 0;
    }
    /**
     * Determines whether {@code state1} is dominated by or equal to {@code state2}.
     * <p>
     * A state {@code state1} is dominated by {@code state2} if all vertices selected in
     * {@code state1} are also selected in {@code state2}.
     * </p>
     *
     * @param state1 the first state to compare
     * @param state2 the second state to compare
     * @return {@code true} if {@code state1} is dominated by or equal to {@code state2}, {@code false} otherwise
     */

    @Override
    public boolean isDominatedOrEqual(BitSet state1, BitSet state2) {
        return state1.stream().allMatch(state2::get);
    }
}
