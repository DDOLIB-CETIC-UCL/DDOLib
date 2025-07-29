package org.ddolib.examples.ddo.carseq;

import org.ddolib.ddo.core.heuristics.variable.VariableHeuristic;

import java.util.Iterator;
import java.util.Set;

public class CSVariableHeuristic implements VariableHeuristic<CSState> {
    private final CSProblem problem;

    public CSVariableHeuristic(CSProblem problem) {
        this.problem = problem;
    }

    @Override
    public Integer nextVariable(Set<Integer> variables, Iterator<CSState> states) {
        if (!states.hasNext()) return 0;

        // Ensure that the variables are assigned in order
        CSState state = states.next();
        return problem.nbVars() - state.nToBuild;
    }
}
