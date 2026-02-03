package org.ddolib.examples.mks;

import org.ddolib.ddo.core.heuristics.variable.VariableHeuristic;
import org.ddolib.examples.knapsack.KSProblem;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;

public class MKSHeuristic implements VariableHeuristic<MKSState> {

    private final Iterator<Integer> ordering;

    public MKSHeuristic(MKSProblem problem) {
        Integer[] orderedVariables = new Integer[problem.nbVars()];
        for (int i = 0; i < problem.nbVars(); i++) {
            orderedVariables[i] = i;
        }
        double[] sumWeights = new double[problem.nbVars()];
        for (int item =0; item < problem.nbVars(); item++) {
            sumWeights[item] = 0;
            for (int dim = 0; dim < problem.weights[item].length; dim++) {
                sumWeights[item] += problem.weights[item][dim];
            }
        }
        Arrays.sort(orderedVariables, Comparator.comparingDouble(x -> -((double) problem.profit[x])/sumWeights[x]));
        ordering = Arrays.stream(orderedVariables).iterator();
    }

    @Override
    public Integer nextVariable(Set<Integer> variables, Iterator<MKSState> states) {
        return ordering.next();
    }

}
