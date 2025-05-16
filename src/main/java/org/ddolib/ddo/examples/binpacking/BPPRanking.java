package org.ddolib.ddo.examples.binpacking;

import org.ddolib.ddo.heuristics.StateRanking;

public class BPPRanking implements StateRanking<BPPState> {
    @Override
    public int compare(BPPState o1, BPPState o2) {
        int nbBinsComparison = Integer.compare(o1.bins.size(), o2.bins.size());
        if(nbBinsComparison != 0) return nbBinsComparison;

        int totRemainingSpaceO1 = o1.bins.stream().map(Bin::remainingSpace).reduce(0, Integer::sum);
        int totRemainingSpaceO2 = o2.bins.stream().map(Bin::remainingSpace).reduce(0, Integer::sum);

        return Integer.compare(totRemainingSpaceO1, totRemainingSpaceO2);
    }

    @Override
    public boolean equals(Object obj) {
        return false;
    }
}
