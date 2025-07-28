package org.ddolib.examples.ddo.smic;

import org.ddolib.ddo.heuristics.StateDistance;

public class SMICDistance implements StateDistance<SMICState> {

    private double distOnRemainingJobs(SMICState a, SMICState b) {
        SMICState smaller = a.getRemainingJobs().size() < b.getRemainingJobs().size() ? a : b;
        SMICState larger = a.getRemainingJobs().size() < b.getRemainingJobs().size() ? b : a;

        int intersectionSize = 0;
        for (int elem: smaller.getRemainingJobs()) {
            if(larger.getRemainingJobs().contains(elem)) {
                intersectionSize++;
            }
        }
        return Math.abs(a.getRemainingJobs().size() + b.getRemainingJobs().size() - 2 * intersectionSize);
    }

    private double distOnCurrentTime(SMICState a, SMICState b) {
        return Math.abs(a.getCurrentTime() - b.getCurrentTime());
    }

    private double distOnMinCurrentInventory(SMICState a, SMICState b) {
        return Math.abs(a.getMinCurrentInventory() - b.getMinCurrentInventory());
    }

    private double distOnMaxCurrentInventory(SMICState a, SMICState b) {
        return Math.abs(a.getMaxCurrentInventory() - b.getMaxCurrentInventory());
    }

    @Override
    public double distance(SMICState a, SMICState b) {
        return distOnRemainingJobs(a, b) + distOnCurrentTime(a, b) + distOnMinCurrentInventory(a, b) + distOnMaxCurrentInventory(a, b);
        // return distOnRemainingJobs(a, b);
        // return distOnCurrentTime(a, b);
        // return distOnMinCurrentInventory(a, b);
        //return distOnMaxCurrentInventory(a, b);
    }
}
