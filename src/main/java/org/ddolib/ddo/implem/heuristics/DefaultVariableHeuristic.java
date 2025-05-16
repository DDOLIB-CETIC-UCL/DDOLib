package org.ddolib.ddo.implem.heuristics;


import org.ddolib.ddo.heuristics.VariableHeuristic;

import java.util.Iterator;
import java.util.Set;

/** 
 * This class implements a default variable ordering. It offers no guarantee as to what 
 * variable is going to be selected next.
 */

/**
 *
 * @param <T> the type of state
 */
public final class DefaultVariableHeuristic<T> implements VariableHeuristic<T> {

    @Override
    public Integer nextVariable(final Set<Integer> variables, final Iterator<T> states) {
        return variables.iterator().next();
    }
    
}
