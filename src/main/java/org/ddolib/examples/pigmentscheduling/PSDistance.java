package org.ddolib.examples.pigmentscheduling;

import org.ddolib.ddo.core.heuristics.cluster.StateDistance;

public class PSDistance implements StateDistance<PSState> {


    @Override
    public double distance(PSState a, PSState b) {
        double previousDemandsDistance = 0;
        for (int i = 0; i < a.previousDemands.length; i++) {
            previousDemandsDistance += Math.abs(a.previousDemands[i] - b.previousDemands[i]);
        }

        // Distance on "next" might be negligible as it only impact the outgoing edges
        // if you need to perform merge on a layer you should prioritize using prevDemands

        return previousDemandsDistance;
    }
}
