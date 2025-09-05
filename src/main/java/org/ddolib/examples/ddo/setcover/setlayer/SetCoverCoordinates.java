package org.ddolib.examples.ddo.setcover.setlayer;

import org.ddolib.ddo.core.heuristics.cluster.StateCoordinates;

public class SetCoverCoordinates implements StateCoordinates<SetCoverState> {
    final private int nElem;

    public SetCoverCoordinates(SetCoverProblem problem) {
        nElem = problem.nElem;
    }

    @Override
    public double[] getCoordinates(SetCoverState state) {
        double[] coords = new double[nElem];
        for (Integer elem: state.uncoveredElements.keySet())
            coords[elem] = 1;
        return coords;
    }
}
