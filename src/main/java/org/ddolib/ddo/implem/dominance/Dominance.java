package org.ddolib.ddo.implem.dominance;

import java.util.Comparator;


public interface Dominance<T,K> {
    K getKey(T state);
    boolean isDominatedOrEqual(T state1, T state2);
}
