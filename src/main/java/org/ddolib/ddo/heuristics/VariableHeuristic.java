package org.ddolib.ddo.heuristics;

import java.util.Iterator;
import java.util.Set;

/**
 * A variable heuristic is used to determine the next variable to branch on.
 * To help making its decision, the heuristic is given access to the
 * nodes from the layer that is about to be expanded.
 */

/**
 * @param <T> the type of state.
 */
public interface VariableHeuristic<T> {
    /**
     * @param variables the set of variables that have not been affected yet
     * @param states    the set of states in the next layer
     * @return The next variable to branch on or null if no decision can be
     * made about any of the states in the next layer
     */
    Integer nextVariable(final Set<Integer> variables, final Iterator<T> states);
}
