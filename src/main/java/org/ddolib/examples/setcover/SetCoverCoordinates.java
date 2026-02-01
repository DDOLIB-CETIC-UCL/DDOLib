package org.ddolib.examples.setcover;

import org.ddolib.ddo.core.heuristics.cluster.StateCoordinates;

public class SetCoverCoordinates implements StateCoordinates<SetCoverState> {
    final private int nElem;

    public SetCoverCoordinates(SetCoverProblem problem) {
        nElem = problem.nItems;
    }

    @Override
    public double[] getCoordinates(SetCoverState state) {
        double[] coords = new double[nElem];
        for (int i = 0; i < nElem; i++) {
            if (state.uncoveredItems().get(i)) {
                coords[i] = 1;
            }
        }
        return coords;
    }
}
