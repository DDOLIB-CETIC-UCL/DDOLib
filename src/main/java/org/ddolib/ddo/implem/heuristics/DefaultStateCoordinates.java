package org.ddolib.ddo.implem.heuristics;

import org.ddolib.ddo.heuristics.StateCoordinates;

public class DefaultStateCoordinates<T> implements StateCoordinates<T> {
    @Override
    public double[] getCoordinates(T state) {
        return new double[0];
    }
}
