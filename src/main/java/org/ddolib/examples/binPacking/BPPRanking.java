package org.ddolib.examples.binPacking;

import org.ddolib.modeling.StateRanking;

public class BPPRanking implements StateRanking<BPPState> {
    @Override
    public int compare(BPPState o1, BPPState o2) {
        // Sort bin fullest to emptiest, therefore lastRemainingSpace should increase.
        return o2.lastRemainingSpace() - o1.lastRemainingSpace();
    }

    @Override
    public boolean equals(Object obj) {
        return false;
    }
}
