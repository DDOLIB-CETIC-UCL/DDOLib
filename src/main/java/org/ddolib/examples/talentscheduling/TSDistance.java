package org.ddolib.examples.talentscheduling;

import static java.lang.Math.max;
import org.ddolib.ddo.core.heuristics.cluster.StateDistance;

import java.util.BitSet;

public class TSDistance implements StateDistance<TSState> {

    final private TSProblem problem;

    public TSDistance(TSProblem problem) {
        this.problem = problem;
    }

    private double distanceOnRemaining(TSState a, TSState b) {
        double distance = 0;
        for (int i = 0; i < problem.nbVars(); i++) {
            if (a.remainingScenes().get(i) != b.remainingScenes().get(i)) {
                distance += problem.sceneCost(i);
            }
        }
        return distance;
    }

    private double distanceOnMaybe(TSState a, TSState b) {
        double distance = 0;
        for (int i = 0; i < problem.nbVars(); i++) {
            if (a.remainingScenes().get(i) != b.remainingScenes().get(i) &&
                    !a.maybeScenes().get(i) && !b.maybeScenes().get(i)) {
                distance++;
            }
        }

        return distance;
    }

    private double distanceOnActors(TSState a, TSState b) {
        double distance = 0;
        BitSet presentActors = problem.onLocationActors(a);
        BitSet presentActors2 = problem.onLocationActors(b);
        presentActors.andNot(problem.onLocationActors(b));
        presentActors2.andNot(problem.onLocationActors(a));
        presentActors.or(presentActors2);

        for (int i = presentActors.nextSetBit(0); i >= 0; i = presentActors.nextSetBit(i + 1)) {
            distance += problem.costs[i];
        }

        return distance;
    }

    @Override
    public double distance(TSState a, TSState b) {
        return distanceOnActors(a, b);
    }

}
