package org.ddolib.examples.salbp;

import org.ddolib.modeling.AcsModel;
import org.ddolib.modeling.FastLowerBound;
import org.ddolib.modeling.Problem;
import org.ddolib.util.verbosity.VerbosityLevel;

public class SALBPAcsModel implements AcsModel<SALBPState> {

    private final SALBProblem problem;

    public SALBPAcsModel(SALBProblem problem) {
        this.problem = problem;
    }

    @Override
    public Problem<SALBPState> problem() {
        return problem;
    }

    @Override
    public FastLowerBound<SALBPState> lowerBound() {
        return new SALBPFastLowerBound(problem);
    }

    @Override
    public VerbosityLevel verbosityLevel() {
        return VerbosityLevel.NORMAL;
    }

    @Override
    public int columnWidth() {
        return 20;
    }
}