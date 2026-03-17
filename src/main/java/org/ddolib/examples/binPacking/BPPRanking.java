package org.ddolib.examples.binPacking;

import org.ddolib.modeling.StateRanking;

public class BPPRanking implements StateRanking<BPPState> {
    @Override
    public int compare(BPPState o1, BPPState o2) {
        if(o2.usedBins == o1.usedBins){
            return o1.remainingItems.stream().sum() - o2.remainingItems.stream().sum();
            //return o1.currentBinSpace - o2.currentBinSpace;
        }
        return o2.usedBins - o1.usedBins;
    }

    @Override
    public boolean equals(Object obj) {
        return false;
    }
}
