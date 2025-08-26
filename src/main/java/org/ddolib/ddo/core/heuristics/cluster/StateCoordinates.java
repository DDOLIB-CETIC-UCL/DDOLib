package org.ddolib.ddo.core.heuristics.cluster;

public interface StateCoordinates<T> {
    /** Computes the coordinates of the state  */
    public double[] getCoordinates(T state);
}
