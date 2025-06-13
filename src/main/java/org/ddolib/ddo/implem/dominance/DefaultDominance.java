package org.ddolib.ddo.implem.dominance;

/**
 * Default implementation of dominance, where no states are dominated.
 * <p>
 * Used by {@link DefaultDominanceChecker}.
 *
 * @param <T> The type of states.
 */
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
