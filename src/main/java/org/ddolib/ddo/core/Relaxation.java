package org.ddolib.ddo.core;

import java.util.Iterator;
import java.util.Set;

/**
 * This is the second most important abstraction that a client should provide 
 * when using this library. It defines the relaxation that may be applied to 
 * the given problem. In particular, the merge_states method from this trait 
 * defines how the nodes of a layer may be combined to provide an upper bound 
 * approximation standing for an arbitrarily selected set of nodes.
 * 
 * Again, the type parameter <T> denotes the type of the states.
 */
public interface Relaxation<T> {
    /** 
     * Merges the given states to create a NEW state which is an over
     * approximation of all the covered states.
     * 
     * @param states the set of states that must be merged
     * @return a new state which is an over approximation of all the considered `states`.
     */
    T mergeStates(final Iterator<T> states);

    /**
     * Relaxes the edge that used to go from `from` to `to` and computes the cost
     * of the new edge going from `from` to `merged`. The decision which is being
     * relaxed is given by `d` and the value of the not relaxed arc is `cost`.
     * 
     * @param from the origin of the relaxed arc
     * @param to the destination of the relaxed arc (before relaxation)
     * @param merged the destination of the relaxed arc (after relaxation)
     * @param d the decision which is being challenged
     * @param cost the cost of the not relaxed arc which used to go from `from` to `to`
     * @return
     */
    int relaxEdge(final T from, final T to, final T merged, final Decision d, final int cost);

    /**
     * @return a very rough estimation (upper bound) of the optimal value that could be
     *  reached if state were the initial state
     * 
     * @param state the state for which the estimate is to be computed
     * @param variable the set of unassigned variables
     */
    default int fastUpperBound(final T state, final Set<Integer> variables) { 
        return Integer.MAX_VALUE;
    };
}
