package org.ddolib.ddo.examples.msct;

import org.ddolib.ddo.modeling.Dominance;

public class MSCTDominance implements Dominance<MSCTState, Integer> {
    @Override
    public Integer getKey(MSCTState state) {
        return 0;
    }

    @Override
    public boolean isDominatedOrEqual(MSCTState state1, MSCTState state2) {
        if (state1.getRemainingJobs().equals(state2.getRemainingJobs()) && state2.getCurrentTime() <= state1.getCurrentTime()) {
            return true;
        }
        return false;
    }
}
