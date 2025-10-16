package org.ddolib.modeling;

public interface DominanceKey<T, K> {

    K value(T state);

}
