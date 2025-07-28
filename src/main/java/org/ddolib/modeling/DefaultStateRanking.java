package org.ddolib.modeling;

/**
 * Default state ranking where all states are equal
 */
public class DefaultStateRanking<T> implements StateRanking<T> {
    @Override
    public int compare(T o1, T o2) {
        return 0;
    }
}
