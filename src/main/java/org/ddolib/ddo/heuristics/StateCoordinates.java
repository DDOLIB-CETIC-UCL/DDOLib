package org.ddolib.ddo.heuristics;

public interface StateCoordinates<T> {
    /** Computes the coordinates of the state  */
    public double[] getCoordinates(T state);
}
