package org.ddolib.examples.binpacking;

import org.ddolib.modeling.StateRanking;

public class BPPRanking implements StateRanking<BPPState> {
    @Override
    public int compare(BPPState o1, BPPState o2) {
        // Sort bin fullest to emptiest, therefore lastRemainingSpace should increase.
        boolean sameLRS = o1.lastRemainingSpace() == o2.lastRemainingSpace();
        boolean sameCBS = o1.currentBinSpace() == o2.currentBinSpace();

        if (sameLRS) {
            if (sameCBS) {
                //small indices == big item ==> keep smallest sum
                return o2.remainingItems().stream().sum() - o1.remainingItems().stream().sum();
            } else {
                return o1.currentBinSpace() - o2.currentBinSpace();
            }
        } else {
            return o2.lastRemainingSpace() - o1.lastRemainingSpace();
        }
    }
}
