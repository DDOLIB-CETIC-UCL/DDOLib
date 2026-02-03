package org.ddolib.examples.knapsack;

import org.ddolib.ddo.core.heuristics.variable.VariableHeuristic;
import org.ddolib.examples.setcover.SetCoverProblem;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;

public class KSHeuristic implements VariableHeuristic<Integer> {
    private final Iterator<Integer> ordering;

    public KSHeuristic(KSProblem problem) {
        Integer[] orderedVariables = new Integer[problem.nbVars()];
        for (int i = 0; i < problem.nbVars(); i++) {
            orderedVariables[i] = i;
        }
        Arrays.sort(orderedVariables, Comparator.comparingDouble(x -> -((double) problem.profit[x])/problem.weight[x]));
        ordering = Arrays.stream(orderedVariables).iterator();
    }

    @Override
    public Integer nextVariable(Set<Integer> variables, Iterator<Integer> states) {
        return ordering.next();
    }
}
