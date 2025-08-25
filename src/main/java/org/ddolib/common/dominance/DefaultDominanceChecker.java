package org.ddolib.common.dominance;

import org.ddolib.modeling.DefaultDominance;

/**
 * A default implementation of dominance checker that checks nothing. It is used when we don't want to activate the
 * dominance checking.
 *
 * @param <T> The type of states.
 */
public class DefaultDominanceChecker<T> extends DominanceChecker<T, Integer> {

    public String getStatistics(){
        return "dominance:NoDominance";
    }

    public DefaultDominanceChecker() {
        super(new DefaultDominance<>());
    }

    @Override
    public boolean updateDominance(T state, int depth, double objValue) {
        return false;
    }
}
