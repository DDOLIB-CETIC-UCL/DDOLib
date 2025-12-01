package org.ddolib.ddo.core.heuristics.cluster;

import org.ddolib.ddo.core.mdd.NodeSubProblem;

public interface StateCoordinates<T> {
    /** Computes the coordinates of the state  */
    double[] getCoordinates(T state);

    default double[] getCoordinates(NodeSubProblem<T> node) {
        return new double[0];
    }
}
