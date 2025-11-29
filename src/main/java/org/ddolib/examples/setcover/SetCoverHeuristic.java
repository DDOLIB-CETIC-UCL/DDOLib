package org.ddolib.examples.setcover;

import org.ddolib.ddo.core.heuristics.variable.VariableHeuristic;

import java.util.*;

public class SetCoverHeuristic implements VariableHeuristic<SetCoverState> {
    private final SetCoverProblem problem;
    private final Iterator<Integer> ordering;

    /**
     * This heuristic orders the items are ordered following their centrality.
     * Elements with a small centrality are harder to cover and thus have a stronger priority.
     * @param problem
     */
    public SetCoverHeuristic(SetCoverProblem problem) {
        this.problem = problem;
        Integer[] orderedVariables = new Integer[problem.nItems];
        for (int i = 0; i < problem.nItems; i++) {
            orderedVariables[i] = i;
        }
        Arrays.sort(orderedVariables, Comparator.comparingInt(x -> this.problem.constraints.get(x).size()));
        ordering = Arrays.stream(orderedVariables).iterator();

    }

    @Override
    public Integer nextVariable(Set<Integer> variables, Iterator<SetCoverState> states) {
        return ordering.next();
    }
}
