package org.ddolib.examples.nolayer.gruler;

import org.ddolib.nolayer.modeling.FastLowerBound;

public class GRFlb implements FastLowerBound<GRState> {

    private final GRProblem problem;

    public GRFlb(GRProblem problem) {
        this.problem = problem;
    }

    @Override
    public double fastLowerBound(GRState state) {
        int missingMarks = problem.order() - state.getNumberOfMarks();

        int i = 0;
        int cost = 0;
        while (missingMarks != 0) {
            if (i != 0 && !state.getDistances().get(i)) {
                cost += i;
                missingMarks--;
            }
            i++;
        }
        return cost;
    }
}
