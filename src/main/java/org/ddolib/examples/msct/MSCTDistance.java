package org.ddolib.examples.msct;

import org.ddolib.ddo.core.heuristics.cluster.StateDistance;

public class MSCTDistance implements StateDistance<MSCTState> {

    private double distOnRemainingJobs(MSCTState a, MSCTState b) {
        MSCTState smaller = a.remainingJobs.size() < b.remainingJobs.size() ? a : b;
        MSCTState larger = a.remainingJobs.size() < b.remainingJobs.size() ? b : a;

        int intersectionSize = 0;
        for (int elem: smaller.remainingJobs) {
            if(larger.remainingJobs.contains(elem)) {
                intersectionSize++;
            }
        }
        return a.remainingJobs.size() + b.remainingJobs.size() - 2 * intersectionSize;
    }

    private double distOnCurrentTime(MSCTState a, MSCTState b) {
        return Math.abs(a.currentTime - b.currentTime);
    }

    @Override
    public double distance(MSCTState a, MSCTState b) {
        return distOnRemainingJobs(a, b) + distOnCurrentTime(a, b);
    }
}
