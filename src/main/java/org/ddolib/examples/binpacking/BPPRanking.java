package org.ddolib.examples.binpacking;

import org.ddolib.modeling.StateRanking;

public class BPPRanking implements StateRanking<BPPState> {
    @Override
    public int compare(BPPState o1, BPPState o2) {
        int o1RemainingSpace = o1.remainingSpace;
        int o2RemainingSpace = o2.remainingSpace;
        if(o1RemainingSpace == -1){
            o1RemainingSpace = o1.remainingSpaces.stream().min(Integer::compareTo).orElse(0);
        }

        if(o2RemainingSpace == -1){
            o2RemainingSpace = o2.remainingSpaces.stream().min(Integer::compareTo).orElse(0);
        }
        int o1Rank = (o1RemainingSpace*o1.usedBins);
        int o2Rank = (o2RemainingSpace*o2.usedBins);
        return Integer.compare(o1Rank, o2Rank);
    }

    @Override
    public boolean equals(Object obj) {
        return false;
    }
}
