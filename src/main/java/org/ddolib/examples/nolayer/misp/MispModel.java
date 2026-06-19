package org.ddolib.examples.nolayer.misp;

import org.ddolib.modeling.nolayer.NoLayerFastLowerBound;
import org.ddolib.modeling.nolayer.NoLayerModel;
import org.ddolib.modeling.nolayer.NoLayerProblem;

public class MispModel implements NoLayerModel<MispState> {

    private final MispProblem problem;
    private final NoLayerFastLowerBound<MispState> lowerBound;

    public MispModel(MispProblem problem) {
        this.problem = problem;
        this.lowerBound = state -> {
            double lb = 0;
            int n = state.remainingNodes().nextSetBit(0);
            while (n >= 0) {
                lb += -problem.weight[n];
                n = state.remainingNodes().nextSetBit(n + 1);
            }
            return lb;
        };
    }

    @Override
    public NoLayerProblem<MispState> problem() {
        return problem;
    }

    @Override
    public NoLayerFastLowerBound<MispState> lowerBound() {
        return lowerBound;
    }
}
