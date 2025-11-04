package org.ddolib.examples.gruler;

import org.ddolib.ddo.core.heuristics.cluster.StateDistance;

public class GRDistance implements StateDistance<GRState> {

    private double distOnLastMark(GRState a, GRState b) {
        return Math.abs(a.getLastMark() - b.getLastMark());
    }

    private double distOnMarks(GRState a, GRState b) {
        double distance = 0;
        int maxMark = Math.max(a.getLastMark(), b.getLastMark());
        for (int i = 0; i < maxMark; i++) {
            if (a.getMarks().get(i) != b.getMarks().get(i) ) {
                distance++;
            }
        }
        return distance;
    }

    private double distOnDistanceSet(GRState a, GRState b) {
        double distance = 0;
        int maxDistance = Math.max(a.getDistances().length(), b.getDistances().length());
        for (int i = 0; i < maxDistance; i++) {
            if (a.getDistances().get(i) != b.getDistances().get(i)) {
                distance++;
            }
        }
        return distance;
    }


    @Override
    public double distance(GRState a, GRState b) {
        return distOnLastMark(a, b);
    }
}
