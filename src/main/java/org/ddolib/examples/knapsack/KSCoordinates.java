package org.ddolib.examples.knapsack;

import org.ddolib.ddo.core.heuristics.cluster.StateCoordinates;

public class KSCoordinates implements StateCoordinates<Integer> {

    @Override
    public double[] getCoordinates(Integer state) {
        return new double[]{state};
    }

}
