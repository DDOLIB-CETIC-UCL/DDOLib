package org.ddolib.examples.gruler;

import org.ddolib.modeling.*;
import org.ddolib.util.testbench.ProblemTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class GRTest {

    private static class GRBench extends ProblemTestBench<GRState, GRProblem> {

        public GRBench() {
            super();
        }

        @Override
        protected List<GRProblem> generateProblems() {
            // Known solutions
            int[] solutions = {0, 1, 3, 6, 11, 17, 25, 34, 44, 55, 72, 85, 106};
            return IntStream.range(1, 7).mapToObj(i -> new GRProblem(i, -solutions[i - 1])).toList();
        }


        @Override
        protected DdoModel<GRState> model(GRProblem problem) {
            return new DdoModel<>() {
                @Override
                public Problem<GRState> problem() {
                    return problem;
                }

                @Override
                public Relaxation<GRState> relaxation() {
                    return new GRRelax();
                }

                @Override
                public StateRanking<GRState> ranking() {
                    return new GRRanking();
                }

                @Override
                public VerbosityLevel verbosityLevel() {
                    return VerbosityLevel.SILENT;
                }
            };
        }
    }

    @DisplayName("Golomb ruler")
    @TestFactory
    public Stream<DynamicTest> testGR() {
        var bench = new GRBench();
        bench.testRelaxation = true;
        return bench.generateTests();
    }
}
