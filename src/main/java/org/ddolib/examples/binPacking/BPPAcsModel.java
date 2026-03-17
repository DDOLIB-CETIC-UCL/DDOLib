package org.ddolib.examples.binPacking;

import org.ddolib.modeling.AcsModel;
import org.ddolib.modeling.FastLowerBound;
import org.ddolib.modeling.Problem;
import org.ddolib.util.verbosity.VerbosityLevel;

public class BPPAcsModel implements AcsModel<BPPState> {

    private final BPPProblem problem;
    private final int WIDTH;
    private final BPPRanking ranking = new BPPRanking();

    public BPPAcsModel(BPPProblem problem, int width) {
        this.problem = problem;
        this.WIDTH = width;
    }


    @Override
    public Problem<BPPState> problem() {
        return problem;
    }

    @Override
    public FastLowerBound<BPPState> lowerBound() {
        return new BPPFastLowerBound(problem);
    }

    @Override
    public VerbosityLevel verbosityLevel() {
        return VerbosityLevel.NORMAL;
    }
}
