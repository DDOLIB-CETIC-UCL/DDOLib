package org.ddolib.examples.grulernolayer;

import org.ddolib.modeling.nolayer.NoLayerFastLowerBound;
import org.ddolib.modeling.nolayer.NoLayerModel;
import org.ddolib.modeling.nolayer.NoLayerProblem;

public class GRNoLayerModel implements NoLayerModel<GRNoLayerState> {

    private final GRNoLayerProblem problem;
    private final NoLayerFastLowerBound<GRNoLayerState> lowerBound;

    public GRNoLayerModel(GRNoLayerProblem problem) {
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
    public NoLayerProblem<GRNoLayerState> problem() {
        return problem;
    }

    @Override
    public NoLayerFastLowerBound<GRNoLayerState> lowerBound() {
        return lowerBound;
    }
}
