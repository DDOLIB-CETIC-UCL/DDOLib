package org.ddolib.examples.tsalt;

import org.ddolib.ddo.core.heuristics.cluster.StateDistance;

import java.util.BitSet;

public class TSDistance implements StateDistance<TSState> {

    final private TSProblem problem;

    public TSDistance(TSProblem problem) {
        this.problem = problem;
    }



    private double distanceOnActors(TSState a, TSState b) {
        double distance = 0;

        for (int i = 0; i < problem.nbActors; i++) {
            if (a.onLocationActors().get(i) != b.onLocationActors().get(i)) {
                distance += problem.costs[i];
            }
        }

        return distance;
    }

    @Override
    public double distance(TSState a, TSState b) {
        // if (distanceOnMaybe(a,b) > 0)
        //     return 1000000;
        return distanceOnActors(a, b);
    }

}
