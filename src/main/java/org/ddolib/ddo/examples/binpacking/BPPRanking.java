package org.ddolib.ddo.examples.binpacking;

import org.ddolib.ddo.heuristics.StateRanking;

public class BPPRanking implements StateRanking<BPPState> {
    @Override
    public int compare(BPPState o1, BPPState o2) {
        int nbBinsComparison = Integer.compare(-o1.totalUsedBin(), -o2.totalUsedBin());
        if (nbBinsComparison != 0) return nbBinsComparison;

        return Integer.compare(o1.remainingTotalWeight - o1.remainingSpace(), o2.remainingTotalWeight - o2.remainingSpace());
    }

    @Override
    public boolean equals(Object obj) {
        return false;
    }
}
