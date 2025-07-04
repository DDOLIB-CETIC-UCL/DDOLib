package org.ddolib.ddo.examples.gruler;

import org.ddolib.ddo.core.CutSetType;
import org.ddolib.ddo.core.Frontier;
import org.ddolib.ddo.heuristics.VariableHeuristic;
import org.ddolib.ddo.implem.dominance.DefaultDominanceChecker;
import org.ddolib.ddo.implem.dominance.DominanceChecker;
import org.ddolib.ddo.implem.frontier.SimpleFrontier;
import org.ddolib.ddo.implem.heuristics.DefaultVariableHeuristic;
import org.ddolib.ddo.implem.heuristics.FixedWidth;
import org.ddolib.ddo.util.testbench.ProblemTestBench;
import org.ddolib.ddo.util.testbench.SolverConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class GRTest {

    private static class GRBench extends ProblemTestBench<GRState, Integer, GRProblem> {

        /**
         * Instantiate a test bench.
         *
         * @param testRelaxation Whether the relaxation must be tested.
         * @param testFUB        Whether the fast upper bound must be tested.
         * @param testDominance  Whether the dominance must be tested.
         */
        public GRBench(boolean testRelaxation, boolean testFUB, boolean testDominance) {
            super(testRelaxation, testFUB, testDominance);
        }

        @Override
        protected List<GRProblem> generateProblems() {
            // Known solutions
            int[] solutions = {0, 1, 3, 6, 11, 17, 25, 34, 44, 55, 72, 85, 106};
            return IntStream.range(1, 7).mapToObj(i -> new GRProblem(i, solutions[i - 1])).toList();
        }

        @Override
        protected SolverConfig<GRState, Integer> configSolver(GRProblem problem) {
            GRRelax relax = new GRRelax();
            GRRanking ranking = new GRRanking();
            FixedWidth<GRState> width = new FixedWidth<>(32);
            VariableHeuristic<GRState> varh = new DefaultVariableHeuristic<>();
            Frontier<GRState> frontier = new SimpleFrontier<>(ranking, CutSetType.LastExactLayer);
            DominanceChecker<GRState, Integer> dominance = new DefaultDominanceChecker<>();
            return new SolverConfig<>(relax, varh, ranking, width, frontier, dominance);
        }
    }

    @DisplayName("Golomb ruler")
    @TestFactory
    public Stream<DynamicTest> testGR() {
        var bench = new GRBench(true, false, false);
        return bench.generateTests();
    }
}
