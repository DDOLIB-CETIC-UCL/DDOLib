package org.ddolib.examples.layered.maximumcoverage;

import java.nio.file.Path;
import org.ddolib.common.util.debug.DebugLevel;
import org.ddolib.common.util.verbosity.VerbosityLevel;
import org.ddolib.layered.modeling.DdoModel;
import org.ddolib.layered.modeling.DefaultDominanceChecker;
import org.ddolib.layered.modeling.DominanceChecker;
import org.ddolib.layered.modeling.FastLowerBound;
import org.ddolib.layered.modeling.Problem;
import org.ddolib.layered.modeling.Relaxation;
import org.ddolib.layered.solving.ddo.core.heuristics.cluster.GHP;
import org.ddolib.layered.solving.ddo.core.heuristics.cluster.ReductionStrategy;

/**
 * Supplies the maximum coverage instances used by the non-regression tests, with a model relying on
 * cluster-based reduction strategies.
 */
public class MaxCoverNonRegressionTestDataSupplier extends MaxCoverTestDataSupplier {

    /**
     * Creates a supplier reading the instances from the given directory.
     *
     * @param dir the directory containing the instance files
     */
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
