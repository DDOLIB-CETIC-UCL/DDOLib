package org.ddolib.ddo.examples.msct;

import org.ddolib.ddo.heuristics.StateRanking;

class SequencingRanking implements StateRanking<MSCTState> {
    @Override
    public int compare(MSCTState s1, MSCTState s2) {
        return Integer.compare(s1.getCurrentTime(), s2.getCurrentTime());
    }
}