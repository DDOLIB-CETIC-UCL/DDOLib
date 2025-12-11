package org.ddolib.examples.binpacking;

import org.ddolib.modeling.StateRanking;

public class BPPRanking implements StateRanking<BPPState> {
    @Override
    public int compare(BPPState o1, BPPState o2) {
        // If positive we keep o1 and merge o2
        if (o1.usedBins == o2.usedBins) {
            if (o1.wastedSpace == o2.wastedSpace) {
                // We prefer to keep the one with most remaining space and less remaining total weight.
                return  (o2.remainingTotalWeight - o2.remainingSpace) - (o1.remainingTotalWeight - o1.remainingSpace);
            } else {
                return o2.wastedSpace - o1.wastedSpace;
            }
        } else {
            // we prefer to keep the one with less wasted space.
            return o2.usedBins - o1.usedBins;
        }
    }

    @Override
    public boolean equals(Object obj) {
        return false;
    }
}
