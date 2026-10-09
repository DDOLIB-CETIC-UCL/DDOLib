package org.ddolib.examples.layered.gruler;

import java.util.List;
import java.util.stream.IntStream;
import org.ddolib.common.util.verbosity.VerbosityLevel;
import org.ddolib.layered.modeling.DdoModel;
import org.ddolib.layered.modeling.FastLowerBound;
import org.ddolib.layered.modeling.Problem;
import org.ddolib.layered.modeling.Relaxation;
import org.ddolib.layered.modeling.StateRanking;
import org.ddolib.layered.testbench.TestDataSupplier;

/**
 * Supplies the Golomb ruler instances (generated programmatically) and the layered model used to
 * solve them in the tests.
 */
public class GRTestDataSupplier extends TestDataSupplier<GRState, GRProblem> {
    @Override
    protected List<GRProblem> generateProblems() {
        // Known solutions
        int[] solutions = {0, 1, 3, 6, 11, 17, 25, 34, 44, 55, 72, 85, 106};
        return IntStream.range(1, 6).mapToObj(i -> new GRProblem(i, solutions[i - 1])).toList();
    }

    @Override
    protected DdoModel<GRState> model(GRProblem problem) {
        return new DdoModel<>() {
            @Override
            public Problem<GRState> problem() {
                return problem;
            }

            @Override
            public FastLowerBound<GRState> lowerBound() {
                return new GRFastLowerBound();
            }

            @Override
            public VerbosityLevel verbosityLevel() {
                return VerbosityLevel.SILENT;
            }

            @Override
            public Relaxation<GRState> relaxation() {
                return new GRRelax();
            }

            @Override
            public StateRanking<GRState> ranking() {
                return new GRRanking();
            }
        };
    }
}
