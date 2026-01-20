package org.ddolib.examples.binpacking;

import org.ddolib.ddo.core.heuristics.cluster.StateCoordinates;

public class BPPCoordinates implements StateCoordinates<BPPState> {
    private final BPPProblem instance;

    public BPPCoordinates(final BPPProblem instance) {
        this.instance = instance;
    }

    @Override
    public double[] getCoordinates(BPPState state) {
        double[] coordinates = new double[4 + instance.nbItems];
        coordinates[0] = state.usedBins;
        coordinates[1] = state.remainingSpace;
        coordinates[2] = state.wastedSpace;
        coordinates[3] = state.remainingTotalWeight;
        for (int item: state.remainingItems)
            coordinates[4 + item] = 1;

        return coordinates;
    }
}
