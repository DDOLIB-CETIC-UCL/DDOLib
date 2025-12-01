package org.ddolib.examples.knapsack;

import org.ddolib.ddo.core.heuristics.cluster.StateDistance;

public class KSDistance implements StateDistance<Integer> {
    final private KSProblem problem;

    public KSDistance(KSProblem problem) {
        this.problem = problem;
    }

    @Override
    public double distance(Integer a, Integer b) {
        return ((double) Math.abs(a - b) / problem.capa);
    }

    @Override
    public double distanceWithRoot(Integer state) {
        return ((double) state) / problem.capa;
    }

}
