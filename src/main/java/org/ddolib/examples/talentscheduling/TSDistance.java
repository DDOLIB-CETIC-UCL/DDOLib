package org.ddolib.examples.talentscheduling;

import static org.ddolib.util.DistanceUtil.*;
import org.ddolib.ddo.core.heuristics.cluster.StateDistance;

import java.util.BitSet;

public class TSDistance implements StateDistance<TSState> {

    final private TSProblem problem;

    public TSDistance(TSProblem problem) {
        this.problem = problem;
    }

    @Override
    public double distance(TSState a, TSState b) {
        return symmetricDifferenceDistance(a.remainingScenes(), b.remainingScenes());
    }

}
