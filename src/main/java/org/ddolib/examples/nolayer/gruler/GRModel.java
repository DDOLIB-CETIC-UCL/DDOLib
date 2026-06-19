package org.ddolib.examples.nolayer.gruler;

import org.ddolib.modeling.nolayer.FastLowerBound;
import org.ddolib.modeling.nolayer.Model;
import org.ddolib.modeling.nolayer.Problem;

public class GRModel implements Model<GRState> {

    private final GRProblem problem;
    private final FastLowerBound<GRState> lowerBound;

    public GRModel(GRProblem problem) {
        this.problem = problem;
        this.lowerBound = state -> {
            int missingMarks = problem.order - state.getNumberOfMarks();

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
        };
    }

    @Override
    public Problem<GRState> problem() {
        return problem;
    }

    @Override
    public FastLowerBound<GRState> lowerBound() {
        return lowerBound;
    }
}
