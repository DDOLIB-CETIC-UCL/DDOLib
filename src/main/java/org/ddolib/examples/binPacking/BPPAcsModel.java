package org.ddolib.examples.binPacking;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.modeling.AcsModel;
import org.ddolib.modeling.FastLowerBound;
import org.ddolib.modeling.Problem;
import org.ddolib.util.verbosity.VerbosityLevel;

public class BPPAcsModel implements AcsModel<BPPState> {

    private final BPPProblem problem;

    public BPPAcsModel(BPPProblem problem) {
        this.problem = problem;
    }

//    public DominanceChecker<BPPState> dominance() {
//        return new SimpleDominanceChecker<>(new BPPDominance(), problem.nbVars());
//    }

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

    @Override
    public int columnWidth() {
        return 20;
    }
}
