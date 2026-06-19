package org.ddolib.examples.mispnolayer;

import org.ddolib.modeling.nolayer.NoLayerFastLowerBound;
import org.ddolib.modeling.nolayer.NoLayerModel;
import org.ddolib.modeling.nolayer.NoLayerProblem;

public class MispNoLayerModel implements NoLayerModel<MispNoLayerState> {

    private final MispNoLayerProblem problem;
    private final NoLayerFastLowerBound<MispNoLayerState> lowerBound;

    public MispNoLayerModel(MispNoLayerProblem problem) {
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
    public NoLayerProblem<MispNoLayerState> problem() {
        return problem;
    }

    @Override
    public NoLayerFastLowerBound<MispNoLayerState> lowerBound() {
        return lowerBound;
    }
}
