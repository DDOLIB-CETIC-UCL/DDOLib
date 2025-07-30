package org.ddolib.examples.ddo.carseq;

import org.ddolib.ddo.heuristics.StateDistance;

public class CSDistance implements StateDistance<CSState> {
    @Override
    public double distance(CSState a, CSState b) {
        double dist = 0;
        for (int i = 0; i < a.carsToBuild.length; i++) {
            dist += Math.abs(a.carsToBuild[i] - b.carsToBuild[i]);
        }
        return dist;
    }
}
