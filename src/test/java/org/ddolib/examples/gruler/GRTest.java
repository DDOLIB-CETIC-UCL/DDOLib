package org.ddolib.examples.gruler;

import org.ddolib.common.solver.SolverConfig;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.modeling.DefaultFastLowerBound;
import org.ddolib.util.testbench.ProblemTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import javax.lang.model.type.NullType;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class GRTest {

    private static class GRBench extends ProblemTestBench<GRState, NullType, GRProblem> {

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
        protected SolverConfig<GRState, NullType> configSolver(GRProblem problem) {
            SolverConfig<GRState, NullType> config = new SolverConfig<>();
            config.problem = problem;
            config.relax = new GRRelax();
            config.ranking = new GRRanking();
            config.width = new FixedWidth<>(10);
            config.varh = new DefaultVariableHeuristic<>();
            config.flb = new DefaultFastLowerBound<>();
            config.frontier = new SimpleFrontier<>(config.ranking, CutSetType.LastExactLayer);
            return config;
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
