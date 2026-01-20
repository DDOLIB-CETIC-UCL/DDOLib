package org.ddolib.examples.boundedknapsack;

import org.ddolib.ddo.core.heuristics.cluster.StateDistance;

public class BKSDistance implements StateDistance<Integer> {
    final private BKSProblem problem;

    public BKSDistance(BKSProblem instance) {
        this.problem = instance;
    }

    @Override
    public double distance(Integer a, Integer b) {
        return ((double) Math.abs(a - b) / problem.capacity);
    }

    @Override
    public double distanceWithRoot(Integer state) {
        return ((double) state) / problem.capacity;
    }


}
