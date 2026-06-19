package org.ddolib.examples.nolayer.gruler;

import org.ddolib.modeling.nolayer.NoLayerFastLowerBound;
import org.ddolib.modeling.nolayer.NoLayerModel;
import org.ddolib.modeling.nolayer.NoLayerProblem;

public class GRModel implements NoLayerModel<GRState> {

    private final GRProblem problem;
    private final NoLayerFastLowerBound<GRState> lowerBound;

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
    public NoLayerProblem<GRState> problem() {
        return problem;
    }

    @Override
    public NoLayerFastLowerBound<GRState> lowerBound() {
        return lowerBound;
    }
}
