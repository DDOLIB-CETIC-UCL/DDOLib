package org.ddolib.examples.misp;

import org.ddolib.ddo.core.heuristics.cluster.StateCoordinates;

import java.util.BitSet;

public class MispCoordinates implements StateCoordinates<BitSet> {

    final MispProblem instance;

    public MispCoordinates(MispProblem instance) {
        this.instance = instance;
    }

    @Override
    public double[] getCoordinates(BitSet state) {
        double[] coordinates = new double[instance.weight.length];
        for (int i = 0; i < coordinates.length; i++) {
            if (state.get(i)) {
                coordinates[i] = 1;
            } else
                coordinates[i] = 0;
        }
        return coordinates;
    }

}
