package org.ddolib.common.dominance;

public class DefaultNoLayerDominanceChecker<T> implements NoLayerDominanceChecker<T> {
    @Override
    public boolean updateDominance(T state, double value) {
        return false;
    }
}
