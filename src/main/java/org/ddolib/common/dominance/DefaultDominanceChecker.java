package org.ddolib.common.dominance;

import org.ddolib.modeling.Dominance;

/**
 * A default implementation of dominance checker that checks nothing. It is used when we don't want to activate the
 * dominance checking.
 *
 * @param <T> The type of states.
 */
public class DefaultDominanceChecker<T> extends DominanceChecker<T> {

    public DefaultDominanceChecker() {
        super(new Dominance<>() {
            @Override
            public Object getKey(T state) {
                return null;
            }

            @Override
            public boolean isDominatedOrEqual(T state1, T state2) {
                return false;
            }
        });
    }

    @Override
    public boolean updateDominance(T state, int depth, double objValue) {
        return false;
    }

}
