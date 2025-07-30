package org.ddolib.ddo.core.heuristics.variable;

import org.ddolib.modeling.Problem;

import java.util.Iterator;
import java.util.Set;

/**
 * Variable ordering that assigns variables in order (0, 1, ..., n)
 */
public class OrderedVariableHeuristic<T> implements VariableHeuristic<T> {
    private final int n;

    public OrderedVariableHeuristic(Problem<T> problem) {
        n = problem.nbVars();
    }

    @Override
    public Integer nextVariable(Set<Integer> variables, Iterator<T> states) {
        return n - variables.size();
    }
}