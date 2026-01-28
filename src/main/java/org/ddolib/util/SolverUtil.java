package org.ddolib.util;

import org.ddolib.ddo.core.Decision;

import java.util.HashSet;
import java.util.Set;

/**
 * Contains method useful to implements solvers
 */
public final class SolverUtil {


    /**
     * Returns the set of variables not covered by the given set of decisions.
     *
     * @param nbVars the number of variables in the related problem
     * @param path   a set of decision
     * @return the set of variables not covered by the given set of decisions
     */
    public static Set<Integer> unassignedVars(int nbVars, Set<Decision> path) {
        final Set<Integer> set = new HashSet<>(nbVars);
        for (int i = 0; i < nbVars; i++) {
            set.add(i);
        }
        for (Decision d : path) {
            set.remove(d.var());
        }
        return set;
    }
}
