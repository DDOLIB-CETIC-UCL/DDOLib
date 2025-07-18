package org.ddolib.examples.ddo.msct;

import org.ddolib.modeling.StateRanking;

public class MSCTRanking implements StateRanking<MSCTState> {
    @Override
    public int compare(MSCTState s1, MSCTState s2) {
        return Integer.compare(s1.getCurrentTime(), s2.getCurrentTime());
    }
}