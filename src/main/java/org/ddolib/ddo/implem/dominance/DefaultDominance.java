package org.ddolib.ddo.implem.dominance;

public class DefaultDominance<T> implements Dominance<T, Integer> {
    @Override
    public Integer getKey(T state) {
        return 0;
    }

    @Override
    public boolean isDominatedOrEqual(T state1, T state2) {
        return false;
    }
}
