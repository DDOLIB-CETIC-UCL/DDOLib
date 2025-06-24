package org.ddolib.ddo.implem.heuristics;

import org.ddolib.ddo.heuristics.StateDistance;

public class DefaultStateDistance<T> implements StateDistance<T> {
    @Override
    public double distance(T a, T b) {
        return 0;
    }
}
