package org.ddolib.modeling;

public interface Dominance<T> {
    Object getKey(T state);

    boolean isDominatedOrEqual(T state1, T state2);
}
