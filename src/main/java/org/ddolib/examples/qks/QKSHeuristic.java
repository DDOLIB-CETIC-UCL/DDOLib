package org.ddolib.examples.qks;

import org.ddolib.ddo.core.heuristics.variable.VariableHeuristic;
import org.ddolib.examples.knapsack.KSProblem;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;

public class QKSHeuristic implements VariableHeuristic<QKSState> {
    private final Iterator<Integer> ordering;

    public QKSHeuristic(QKSProblem problem) {
        Integer[] orderedVariables = new Integer[problem.nbVars()];
        for (int i = 0; i < problem.nbVars(); i++) {
            orderedVariables[i] = i;
        }
        double[] sumProfit = new double[problem.nbVars()];
        for (int i = 0; i < problem.nbVars(); i++) {
            for (int j = 0; j < problem.profitMatrix[i].length; j++) {
                sumProfit[i] += problem.profitMatrix[i][j];
            }
        }
        Arrays.sort(orderedVariables, Comparator.comparingDouble(x -> -sumProfit[x]/problem.weights[x]));
        ordering = Arrays.stream(orderedVariables).iterator();
    }

    @Override
    public Integer nextVariable(Set<Integer> variables, Iterator<QKSState> states) {
        return ordering.next();
    }
}
