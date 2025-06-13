package org.ddolib.ddo.implem.dominance;

/**
 * A default implementation of dominance checker that checks nothing. It is used when we don't want to activate the
 * dominance checking.
 *
 * @param <T> The type of states.
 */
public class DefaultDominanceChecker<T> extends DominanceChecker<T, Integer> {

    public DefaultDominanceChecker() {
        super(new DefaultDominance<>());
    }

    @Override
    public boolean updateDominance(T state, int depth, int objValue) {
        return false;
    }
}
