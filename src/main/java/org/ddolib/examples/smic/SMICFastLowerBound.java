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
        int minRelease = Integer.MAX_VALUE;
        for (int j : state.remainingJobs()) {
            minRelease = Math.min(minRelease, problem.release[j]);
            lowerBound += problem.processing[j];
        }
        return Math.min(0, minRelease - state.currentTime()) + lowerBound; //minProcessing * variables.size();
    }
}
