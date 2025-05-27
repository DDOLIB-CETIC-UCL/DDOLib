package org.ddolib.ddo.implem.dominance;

public interface DominanceChecker<T, K> {

    boolean updateDominance(T state, int depth, int objValue);
}
