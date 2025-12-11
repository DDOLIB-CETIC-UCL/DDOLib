package org.ddolib.examples.binpacking;

import org.ddolib.modeling.Dominance;

public class BPPDominance implements Dominance<BPPState> {
    @Override
    public Object getKey(BPPState state) {
        return state.remainingTotalWeight;
    }

    @Override
    public boolean isDominatedOrEqual(BPPState state1, BPPState state2) {
        return state1.usedBins >= state2.usedBins &&
                state1.remainingSpace <= state2.remainingSpace;
    }
}

