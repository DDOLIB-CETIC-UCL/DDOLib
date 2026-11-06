package org.ddolib.examples.layered.misp;

import java.util.BitSet;
import org.ddolib.layered.modeling.Dominance;

/**
 * Implementation of a dominance relation for the Maximum Independent Set Problem (MISP).
 *
 * <p>In this context, one state {@code state1} is considered dominated by another state {@code
 * state2} if all vertices selected in {@code state1} are also selected in {@code state2}. This
 * allows pruning suboptimal states during the search.
 */
public class MispDominance implements Dominance<BitSet> {

    /** Creates a new instance of this dominance rule. */
    public MispDominance() {}

    /**
     * Returns a key for the dominance relation.
     *
     * <p>This implementation always returns {@code 0}: all the states of a layer share the same
     * key. A key groups the states that can be compared, and must thus be equal for a state and
     * the states dominating it. With a dominance by inclusion, such states do not share any
     * property that could be used as key (neither the set of vertices nor its size), so that a
     * single group is needed.
     *
     * <p>This has a cost: with a {@link org.ddolib.layered.modeling.SimpleDominanceChecker}, each
     * new state is compared with all the non-dominated states of its layer, i.e. a number of
     * comparisons quadratic in the number of states of a layer, each comparison taking a time
     * linear in the number of vertices.
     *
     * @param state the current state
     * @return a key for the dominance relation, always {@code 0}
     */
    @Override
    public Integer getKey(BitSet state) {
        return 0;
    }

    /**
     * Determines whether {@code state1} is dominated by or equal to {@code state2}.
     *
     * <p>A state {@code state1} is dominated by {@code state2} if all vertices selected in {@code
     * state1} are also selected in {@code state2}.
     *
     * @param state1 the first state to compare
     * @param state2 the second state to compare
     * @return {@code true} if {@code state1} is dominated by or equal to {@code state2}, {@code
     *     false} otherwise
     */
    @Override
    public boolean isDominatedOrEqual(BitSet state1, BitSet state2) {
        return state1.stream().allMatch(state2::get);
    }
}
