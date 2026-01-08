package org.ddolib.examples.maximumcoverage;

import org.ddolib.modeling.Dominance;

import java.util.BitSet;

public class MaxCoverDominance implements Dominance<MaxCoverState> {
    @Override
    public Integer getKey(MaxCoverState state) {
        // All states share the same key (0), meaning they are all comparable for dominance
        return 0;
    }

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
