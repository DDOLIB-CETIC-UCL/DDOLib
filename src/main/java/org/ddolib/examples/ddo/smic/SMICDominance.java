package org.ddolib.examples.ddo.smic;

import org.ddolib.modeling.Dominance;

public class SMICDominance implements Dominance<SMICState, Integer> {
    @Override
    public Integer getKey(SMICState state) {
        return 0;
    }

    @Override
    public boolean isDominatedOrEqual(SMICState state1, SMICState state2) {
        if (state1.getRemainingJobs().equals(state2.getRemainingJobs()) && state2.getCurrentTime() <= state1.getCurrentTime()) {
            return true;
        }
        return false;
    }
}
