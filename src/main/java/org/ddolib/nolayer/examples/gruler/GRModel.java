package org.ddolib.nolayer.examples.gruler;

import org.ddolib.nolayer.modeling.FastLowerBound;
import org.ddolib.nolayer.modeling.Model;
import org.ddolib.nolayer.modeling.Problem;

public class GRModel implements Model<GRState> {

    private final GRProblem problem;
    private final FastLowerBound<GRState> lowerBound;

    public GRModel(GRProblem problem) {
        this.problem = problem;
        this.lowerBound = new GRFlb(problem);
    }

    @Override
    public Problem<GRState> problem() {
        return problem;
    }

    @Override
    public FastLowerBound<GRState> lowerBound() {
        return lowerBound;
    }
}
