package org.ddolib.examples.nolayer.knapsack;

import org.ddolib.modeling.nolayer.FastLowerBound;
import org.ddolib.modeling.nolayer.Model;
import org.ddolib.modeling.nolayer.Problem;

public class KSModel implements Model<KSState> {

    private final KSProblem problem;
    private final FastLowerBound<KSState> lowerBound;

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
    public Problem<KSState> problem() {
        return problem;
    }

    @Override
    public FastLowerBound<KSState> lowerBound() {
        return lowerBound;
    }
}
