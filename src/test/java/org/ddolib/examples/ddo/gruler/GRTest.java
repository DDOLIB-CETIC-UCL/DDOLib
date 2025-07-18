package org.ddolib.examples.ddo.gruler;

import org.ddolib.common.dominance.DefaultDominanceChecker;
import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.Frontier;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.ddo.core.heuristics.variable.VariableHeuristic;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.modeling.DefaultFastUpperBound;
import org.ddolib.modeling.FastUpperBound;
import org.ddolib.util.testbench.ProblemTestBench;
import org.ddolib.util.testbench.SolverConfig;
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
            FastUpperBound<GRState> fub = new DefaultFastUpperBound<>();
            DominanceChecker<GRState, Integer> dominance = new DefaultDominanceChecker<>();
            return new SolverConfig<>(relax, varh, ranking, width, frontier, fub, dominance);
        }
    }

    @DisplayName("Golomb ruler")
    @TestFactory
    public Stream<DynamicTest> testGR() {
        var bench = new GRBench(true, false, false);
        return bench.generateTests();
    }
}
