package org.ddolib.ddo.implem.dominance;

public class DefaultDominanceChecker<T> implements DominanceChecker<T, Integer> {
    @Override
    public boolean updateDominance(T state, int depth, int objValue) {
        return false;
    }
}
