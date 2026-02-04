package org.ddolib.examples.boundedknapsack;

import org.ddolib.ddo.core.heuristics.variable.VariableHeuristic;
import org.ddolib.examples.knapsack.KSProblem;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;

public class BKSHeuristic implements VariableHeuristic<Integer> {
    private final Iterator<Integer> ordering;

    public BKSHeuristic(BKSProblem problem) {
        Integer[] orderedVariables = new Integer[problem.nbVars()];
        for (int i = 0; i < problem.nbVars(); i++) {
            orderedVariables[i] = i;
        }
        Arrays.sort(orderedVariables, Comparator.comparingDouble(x -> -((double) problem.values[x])/problem.weights[x]));
        ordering = Arrays.stream(orderedVariables).iterator();
    }

    @Override
    public Integer nextVariable(Set<Integer> variables, Iterator<Integer> states) {
        return ordering.next();
    }
}
