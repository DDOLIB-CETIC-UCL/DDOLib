package org.ddolib.examples.mks;
import org.ddolib.ddo.core.heuristics.cluster.StateCoordinates;

public class MKSCoordinates implements StateCoordinates<MKSState> {
    @Override
    public double[] getCoordinates(MKSState state) {
        return state.capacities.clone();
    }
}
