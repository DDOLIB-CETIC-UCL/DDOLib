package org.ddolib.examples.knapsack;

import org.ddolib.ddo.core.heuristics.variable.VariableHeuristic;

import java.util.Iterator;
import java.util.Set;

public class KSHeuristic implements VariableHeuristic<Integer> {
    @Override
    public Integer nextVariable(Set<Integer> variables, Iterator<Integer> states) {
        return 0;
    }
}
