package org.ddolib.examples.maximumcoverage;

import org.ddolib.modeling.Dominance;

import java.util.BitSet;
/**
 * Dominance rule for {@link MaxCoverState} used in the Maximum Coverage problem.
 *
 * <p>
 * This class implements the {@link Dominance} interface and defines a dominance
 * relationship between states based on set inclusion:
 * a state is dominated if its set of covered items is a subset of another state's
 * covered items.
 *
 * <p>
 * All states are assigned the same dominance key, meaning that any two states
 * are comparable for dominance checking.
 */
public class MaxCoverDominance implements Dominance<MaxCoverState> {
    /**
     * Returns the dominance key associated with a state.
     *
     * <p>
     * All states share the same key ({@code 0}), which indicates that
     * dominance comparisons are allowed between any pair of states.
     *
     * @param state the state for which the key is requested
     * @return the dominance key (always {@code 0})
     */
    @Override
    public Integer getKey(MaxCoverState state) {
        // All states share the same key (0), meaning they are all comparable for dominance
        return 0;
    }
    /**
     * Determines whether {@code state1} is dominated by or equal to {@code state2}.
     *
     * <p>
     * A state {@code state1} is considered dominated by {@code state2} if
     * the set of items covered by {@code state1} is a subset of the set of
     * items covered by {@code state2}. Equality is included as a special case.
     *
     * @param state1 the potentially dominated state
     * @param state2 the dominating state
     * @return {@code true} if {@code state1} is dominated by or equal to {@code state2},
     *         {@code false} otherwise
     */
    @Override
    public boolean isDominatedOrEqual(MaxCoverState state1, MaxCoverState state2) {
        // state1 is dominated by state2 if the covered items of state1 are a subset of those of state2
        BitSet coveredItems1 = state1.coveredItems();
        BitSet coveredItems2 = state2.coveredItems();

        BitSet temp = (BitSet) coveredItems1.clone();
        temp.andNot(coveredItems2);
        return temp.isEmpty();
    }
}
