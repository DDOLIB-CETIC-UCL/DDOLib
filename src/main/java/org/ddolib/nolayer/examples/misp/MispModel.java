package org.ddolib.nolayer.examples.misp;

import org.ddolib.nolayer.modeling.FastLowerBound;
import org.ddolib.nolayer.modeling.Model;
import org.ddolib.nolayer.modeling.Problem;

public class MispModel implements Model<MispState> {

    private final MispProblem problem;
    private final FastLowerBound<MispState> lowerBound;

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
    public Problem<MispState> problem() {
        return problem;
    }

    @Override
    public FastLowerBound<MispState> lowerBound() {
        return lowerBound;
    }
}
