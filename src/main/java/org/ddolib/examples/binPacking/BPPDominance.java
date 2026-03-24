package org.ddolib.examples.binPacking;

import org.ddolib.modeling.Dominance;

/*public class BPPDominance implements Dominance<BPPState> {

    // Dominance is not working
    // Tried using remainingItems as key, but it was generating way too much key.
    // Java heap space problem with this configuration too.

    @Override
    public Object getKey(BPPState state) {
        return state.usedBins();
    }

    @Override
    public boolean isDominatedOrEqual(BPPState state1, BPPState state2) {
        return state1.lastRemainingSpace() >= state2.lastRemainingSpace();
    }
}*/