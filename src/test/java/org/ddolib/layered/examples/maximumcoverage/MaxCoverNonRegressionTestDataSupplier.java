package org.ddolib.layered.examples.maximumcoverage;

import org.ddolib.layered.modeling.*;
import org.ddolib.layered.solving.ddo.core.heuristics.cluster.GHP;
import org.ddolib.layered.solving.ddo.core.heuristics.cluster.ReductionStrategy;
import org.ddolib.util.debug.DebugLevel;
import org.ddolib.util.verbosity.VerbosityLevel;

import java.nio.file.Path;

public class MaxCoverNonRegressionTestDataSupplier extends MaxCoverTestDataSupplier {

    public MaxCoverNonRegressionTestDataSupplier(Path dir) {
        super(dir);
    }

    @Override
    protected DdoModel<MaxCoverState> model(MaxCoverProblem problem) {
        return new DdoModel<>() {

            @Override
            public Problem<MaxCoverState> problem() {
                return problem;
            }

            @Override
            public Relaxation<MaxCoverState> relaxation() {
                return new MaxCoverRelax(problem);
            }

            @Override
            public MaxCoverRanking ranking() {
                return new MaxCoverRanking();
            }

            @Override
            public FastLowerBound<MaxCoverState> lowerBound() {
                return new MaxCoverFastLowerBound(problem);
            }

            @Override
            public DominanceChecker<MaxCoverState> dominance() {
                return new DefaultDominanceChecker<>();
            }

            @Override
            public VerbosityLevel verbosityLevel() {
                return VerbosityLevel.SILENT;
            }

            @Override
            public DebugLevel debugMode() {
                return DebugLevel.ON;
            }

            @Override
            public ReductionStrategy<MaxCoverState> relaxStrategy() {
                return new GHP<>(new MaxCoverDistance(problem));
            }

            @Override
            public ReductionStrategy<MaxCoverState> restrictStrategy() {
                return new GHP<>(new MaxCoverDistance(problem));
            }
        };
    }
}
