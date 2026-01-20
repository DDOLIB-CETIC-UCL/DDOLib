package org.ddolib.examples.boundedknapsack;

import org.ddolib.ddo.core.heuristics.cluster.StateCoordinates;

public class BKSCoordinates implements StateCoordinates<Integer> {
    @Override
    public double[] getCoordinates(Integer state) {
        return new double[]{state};
    }
}
