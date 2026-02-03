package org.ddolib.examples.misp;

import org.ddolib.ddo.core.heuristics.variable.VariableHeuristic;

import java.util.*;

public class MispHeuristic implements VariableHeuristic<BitSet> {
    private final Iterator<Integer> ordering;

    public MispHeuristic(MispProblem problem) {
        Integer[] orderedVariables = new Integer[problem.nbVars()];
        for (int i = 0; i < problem.nbVars(); i++) {
            orderedVariables[i] = i;
        }
        Arrays.sort(orderedVariables, Comparator.comparingInt(x -> problem.neighbors[x].cardinality()));
        ordering = Arrays.stream(orderedVariables).iterator();
    }

    @Override
    public Integer nextVariable(Set<Integer> variables, Iterator<BitSet> states) {
        return ordering.next();
    }
}
