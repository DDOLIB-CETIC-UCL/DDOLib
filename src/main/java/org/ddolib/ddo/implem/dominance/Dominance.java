package org.ddolib.ddo.implem.dominance;

import java.util.Comparator;


public interface Dominance<S,K> {
    K getKey(S state);
    boolean isDominatedOrEqual(S state1, S state2);
}
