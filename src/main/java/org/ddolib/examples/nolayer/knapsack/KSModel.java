package org.ddolib.examples.nolayer.knapsack;

import org.ddolib.modeling.nolayer.NoLayerFastLowerBound;
import org.ddolib.modeling.nolayer.NoLayerModel;
import org.ddolib.modeling.nolayer.NoLayerProblem;

public class KSModel implements NoLayerModel<KSState> {

    private final KSProblem problem;
    private final NoLayerFastLowerBound<KSState> lowerBound;

    public KSModel(KSProblem problem) {
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
    public NoLayerProblem<KSState> problem() {
        return problem;
    }

    @Override
    public NoLayerFastLowerBound<KSState> lowerBound() {
        return lowerBound;
    }
}
