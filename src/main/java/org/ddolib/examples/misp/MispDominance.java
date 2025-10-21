package org.ddolib.examples.misp;

import org.ddolib.modeling.Dominance;

import java.util.BitSet;

public class MispDominance implements Dominance<BitSet> {
    @Override
    public Integer getKey(BitSet state) {
        return 0;
    }

    @Override
    public boolean isDominatedOrEqual(BitSet state1, BitSet state2) {
        return state1.stream().allMatch(state2::get);
    }
}
