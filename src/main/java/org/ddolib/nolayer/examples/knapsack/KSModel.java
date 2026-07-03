package org.ddolib.nolayer.examples.knapsack;

import org.ddolib.nolayer.modeling.FastLowerBound;
import org.ddolib.nolayer.modeling.Model;
import org.ddolib.nolayer.modeling.Problem;

public class KSModel implements Model<KSState> {

    private final KSProblem problem;
    private final FastLowerBound<KSState> lowerBound;

    public KSModel(KSProblem problem) {
        this.problem = problem;
        this.lowerBound = new KSFlb(problem);
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
