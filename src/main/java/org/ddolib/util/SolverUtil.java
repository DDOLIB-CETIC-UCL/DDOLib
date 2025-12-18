package org.ddolib.util;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.Problem;

import java.util.HashSet;
import java.util.Set;

/**
 * Contains method useful to implements solvers
 */
public final class SolverUtil {


    public static <T> Set<Integer> varSet(Problem<T> problem, Set<Decision> path) {
        final HashSet<Integer> set = new HashSet<>();
        for (int i = 0; i < problem.nbVars(); i++) {
            set.add(i);
        }
        for (Decision d : path) {
            set.remove(d.var());
        }
        return set;
    }
}
