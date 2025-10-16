package org.ddolib.examples.smic;

import org.ddolib.modeling.Dominance;

public class SMICDominance implements Dominance<SMICState> {
    @Override
    public Integer getKey(SMICState state) {
        return 0;
    }

    @Override
    public boolean isDominatedOrEqual(SMICState state1, SMICState state2) {
        if (state1.remainingJobs().equals(state2.remainingJobs()) && state2.currentTime() <= state1.currentTime()) {
            return true;
        }
        return false;
    }
}
