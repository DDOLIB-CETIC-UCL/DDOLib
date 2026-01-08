package org.ddolib.examples.mks;

import org.ddolib.ddo.core.heuristics.cluster.StateDistance;
import static org.ddolib.util.DistanceUtil.euclideanDistance;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

public class MKSDistance implements StateDistance<MKSState> {
    final MKSProblem instance;

    public MKSDistance(MKSProblem instance) {
        this.instance = instance;
    }

    @Override
    public double distance(MKSState a, MKSState b) {
        return euclideanDistance(a.capacities, b.capacities) / instance.maximalDistance;
    }

    @Override
    public double distanceWithRoot(MKSState a) {
        return euclideanDistance(a.capacities, instance.initialState().capacities) / instance.maximalDistance;
    }
}
