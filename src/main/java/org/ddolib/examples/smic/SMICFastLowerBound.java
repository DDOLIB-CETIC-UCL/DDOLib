package org.ddolib.examples.smic;

import org.ddolib.modeling.FastLowerBound;

import java.util.Set;

public class SMICFastLowerBound implements FastLowerBound<SMICState> {
    private final SMICProblem problem;

    SMICFastLowerBound(SMICProblem problem)  {
        this.problem = problem;
    }

    @Override
    public double fastLowerBound(SMICState state, Set<Integer> variables) {
        double lowerBound = 0.0;
        for (Integer variable : variables) {
            lowerBound += problem.processing[variable];
        }

        return lowerBound;
    }
}
