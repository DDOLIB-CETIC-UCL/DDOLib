package org.ddolib.examples.ddo.knapsack;

import org.ddolib.ddo.core.heuristics.cluster.StateDistance;

public class KSDistance implements StateDistance<Integer> {

    @Override
    public double distance(Integer a, Integer b) {
        return Math.abs(a - b);
    }

}
