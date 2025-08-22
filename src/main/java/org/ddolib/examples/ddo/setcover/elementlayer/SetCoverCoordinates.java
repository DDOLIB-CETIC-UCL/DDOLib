package org.ddolib.examples.ddo.setcover.elementlayer;

import org.ddolib.ddo.heuristics.StateCoordinates;

public class SetCoverCoordinates implements StateCoordinates<SetCoverState> {
    final private int nElem;

    public SetCoverCoordinates(SetCoverProblem problem) {
        nElem = problem.nElem;
    }

    @Override
    public double[] getCoordinates(SetCoverState state) {
        double[] coords = new double[nElem];
        for (Integer elem: state.uncoveredElements)
            coords[elem] = 1;
        return coords;
    }
}
