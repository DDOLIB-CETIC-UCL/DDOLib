package org.ddolib.ddo.examples.knapsack;

import org.ddolib.ddo.heuristics.StateDistance;
import smile.math.distance.Distance;

public class KSDistance implements StateDistance<Integer> {
    @Override
    public double distance(Integer a, Integer b) {
        return Math.abs(a - b);
    }
}
