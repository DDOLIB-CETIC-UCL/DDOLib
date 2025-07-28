package org.ddolib.examples.ddo.pigmentscheduling;

import org.ddolib.ddo.heuristics.StateDistance;

public class PSDistance implements StateDistance<PSState> {
    @Override
    public double distance(PSState a, PSState b) {
        return 0;
    }
}
