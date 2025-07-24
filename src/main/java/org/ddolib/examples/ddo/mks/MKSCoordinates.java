package org.ddolib.examples.ddo.mks;

import org.ddolib.ddo.heuristics.StateCoordinates;

public class MKSCoordinates implements StateCoordinates<MKSState> {
    @Override
    public double[] getCoordinates(MKSState state) {
        return state.capacities.clone();
    }
}
