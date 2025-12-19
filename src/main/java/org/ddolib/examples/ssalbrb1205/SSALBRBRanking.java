package org.ddolib.examples.ssalbrb1205;

import org.ddolib.modeling.StateRanking;

public class SSALBRBRanking implements StateRanking<SSALBRBState> {

    @Override
    public int compare(SSALBRBState first, SSALBRBState second) {
        return Integer.compare(first.makespan(), second.makespan());
    }
}
