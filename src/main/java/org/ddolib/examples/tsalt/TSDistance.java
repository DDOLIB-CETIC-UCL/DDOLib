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
        BitSet presentActors = (BitSet) a.onLocationActors().clone();
        BitSet presentActors2 = (BitSet) b.onLocationActors().clone();
        presentActors.andNot(b.onLocationActors());
        presentActors2.andNot(a.onLocationActors());
        presentActors.or(presentActors2);

        for (int i = presentActors.nextSetBit(0); i >= 0; i = presentActors.nextSetBit(i + 1)) {
            distance += problem.costs[i];
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
