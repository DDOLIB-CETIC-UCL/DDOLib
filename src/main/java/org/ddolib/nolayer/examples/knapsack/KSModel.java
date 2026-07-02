package org.ddolib.nolayer.examples.knapsack;

import org.ddolib.nolayer.modeling.FastLowerBound;
import org.ddolib.nolayer.modeling.Model;
import org.ddolib.nolayer.modeling.Problem;

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
                } else if (cap > 0) {
                    double ratio = (double) problem.profit[i] / problem.weight[i];
                    lb += -Math.ceil(ratio * cap);
                    break;
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
