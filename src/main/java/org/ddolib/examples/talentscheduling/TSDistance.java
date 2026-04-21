package org.ddolib.examples.talentscheduling;

import static org.ddolib.util.DistanceUtil.*;
import org.ddolib.ddo.core.heuristics.cluster.StateDistance;

import java.util.BitSet;

/**
 * State distance for Talent Scheduling based on the symmetric difference of remaining scenes.
 */
public class TSDistance implements StateDistance<TSState> {

    final private TSProblem problem;

    /**
     * Creates a distance helper bound to a given Talent Scheduling instance.
     *
     * @param problem target problem instance
     */
    public TSDistance(TSProblem problem) {
        this.problem = problem;
    }

    @Override
    public double distance(TSState a, TSState b) {
        return symmetricDifferenceDistance(a.remainingScenes(), b.remainingScenes());
    }

}
