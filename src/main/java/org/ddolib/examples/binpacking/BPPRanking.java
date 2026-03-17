package org.ddolib.examples.binpacking;

import org.ddolib.modeling.StateRanking;

public class BPPRanking implements StateRanking<BPPState> {
    @Override
    public int compare(BPPState o1, BPPState o2) {
        return o2.usedBins() - o1.usedBins();
    }

    @Override
    public boolean equals(Object obj) {
        return false;
    }
}
