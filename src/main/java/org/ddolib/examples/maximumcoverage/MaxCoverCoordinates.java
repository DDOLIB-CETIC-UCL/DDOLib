package org.ddolib.examples.maximumcoverage;

import org.ddolib.ddo.core.heuristics.cluster.StateCoordinates;

public class MaxCoverCoordinates implements StateCoordinates<MaxCoverState> {

    final private MaxCoverProblem instance;

    public MaxCoverCoordinates(MaxCoverProblem instance) {
        this.instance = instance;
    }

    @Override
    public double[] getCoordinates(MaxCoverState state) {
        double[] coordinates = new double[instance.nbItems];
        for (int i = 0; i < coordinates.length; i++) {
            if (state.coveredItems().get(i))
                coordinates[i] = 1;
            else
                coordinates[i] = 0;
        }
        return coordinates;
    }
}
