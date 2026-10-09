package org.ddolib.examples.layered.talentscheduling;

import static org.ddolib.common.util.DistanceUtil.symmetricDifferenceDistance;

import org.ddolib.layered.solving.ddo.core.heuristics.cluster.StateDistance;

/** State distance for Talent Scheduling based on the symmetric difference of remaining scenes. */
public class TSDistance implements StateDistance<TSState> {

    private final TSProblem problem;

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
