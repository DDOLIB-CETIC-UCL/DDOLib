package org.ddolib.examples.knapsacknolayer;

import org.ddolib.modeling.nolayer.NoLayerFastLowerBound;
import org.ddolib.modeling.nolayer.NoLayerModel;
import org.ddolib.modeling.nolayer.NoLayerProblem;

public class KSNoLayerModel implements NoLayerModel<KSNoLayerState> {

    private final KSNoLayerProblem problem;
    private final NoLayerFastLowerBound<KSNoLayerState> lowerBound;

    public KSNoLayerModel(KSNoLayerProblem problem) {
        this.problem = problem;
        this.lowerBound = state -> {
            double lb = 0;
            int cap = state.remainingCapacity();
            for (int i = state.currentItem(); i < problem.profit.length; i++) {
                if (cap >= problem.weight[i]) {
                    lb += -problem.profit[i];
                    cap -= problem.weight[i];
                }
            }
            return lb;
        };
    }

    @Override
    public NoLayerProblem<KSNoLayerState> problem() {
        return problem;
    }

    @Override
    public NoLayerFastLowerBound<KSNoLayerState> lowerBound() {
        return lowerBound;
    }
}
