package org.ddolib.examples.pigmentscheduling;

import org.ddolib.ddo.core.heuristics.cluster.StateCoordinates;

import java.util.Arrays;
import java.util.stream.Stream;

public class PSCoordinates implements StateCoordinates<PSState> {

    @Override
    public double[] getCoordinates(PSState state) {
        double[] coordinates = new double[state.previousDemands.length + 1];
        for (int i = 0; i < state.previousDemands.length; i++) {
            coordinates[i] = state.previousDemands[i];
        }
        coordinates[coordinates.length - 1] = state.next;
        return coordinates;
    }

}
