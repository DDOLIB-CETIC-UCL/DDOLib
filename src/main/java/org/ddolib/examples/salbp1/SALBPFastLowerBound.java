package org.ddolib.examples.salbp1;

import org.ddolib.modeling.FastLowerBound;

import java.util.Set;

public class SALBPFastLowerBound implements FastLowerBound<SALBPState> {

    final SALBPProblem problem;
    public SALBPFastLowerBound(SALBPProblem problem) {
        this.problem = problem;
    }

    @Override
    public double fastLowerBound(SALBPState state, Set<Integer> variables) {
        double durationSum = 0;
        for (Integer variable : variables) {
            durationSum += problem.durations[variable];
        }
        return Math.ceil((durationSum - state.remainingDuration())/problem.cycleTime);
    }
}
