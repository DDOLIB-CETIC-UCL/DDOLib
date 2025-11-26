package org.ddolib.examples.gruler;

import org.ddolib.modeling.DdoModel;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Relaxation;
import org.ddolib.modeling.StateRanking;
import org.ddolib.util.testbench.TestUnit;
import org.ddolib.util.verbosity.VerbosityLevel;

import java.util.List;
import java.util.stream.IntStream;

public class GRTestUnit extends TestUnit<GRState, GRProblem> {
    @Override
    protected List<GRProblem> generateProblems() {
        // Known solutions
        int[] solutions = {0, 1, 3, 6, 11, 17, 25, 34, 44, 55, 72, 85, 106};
        return IntStream.range(1, 7).mapToObj(i -> new GRProblem(i, solutions[i - 1])).toList();
    }

    @Override
    protected DdoModel<GRState> model(GRProblem problem) {
        return new DdoModel<>() {
            @Override
            public Problem<GRState> problem() {
                return problem;
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
