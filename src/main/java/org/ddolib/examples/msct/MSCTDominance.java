package org.ddolib.examples.msct;

import org.ddolib.modeling.Dominance;

public class MSCTDominance implements Dominance<MSCTState> {
    @Override
    public Integer getKey(MSCTState state) {
        return 0;
    }

    @Override
    public boolean isDominatedOrEqual(MSCTState state1, MSCTState state2) {
        if (state1.remainingJobs().equals(state2.remainingJobs()) && state2.currentTime() <= state1.currentTime()) {
            return true;
        }
        return false;
    }
}
