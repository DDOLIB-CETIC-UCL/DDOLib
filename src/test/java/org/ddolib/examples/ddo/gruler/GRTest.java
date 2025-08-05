package org.ddolib.examples.ddo.gruler;

import org.ddolib.common.dominance.DefaultDominanceChecker;
import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.Frontier;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.ddo.core.heuristics.variable.VariableHeuristic;
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

        public GRBench() {
            super(true, false, false);
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
            VariableHeuristic<GRState> varh = new DefaultVariableHeuristic<>();
            Frontier<GRState> frontier = new SimpleFrontier<>(ranking, CutSetType.LastExactLayer);
            FastUpperBound<GRState> fub = new DefaultFastUpperBound<>();
            DominanceChecker<GRState, Integer> dominance = new DefaultDominanceChecker<>();
            return new SolverConfig<>(relax, varh, ranking, 2, 20, frontier, fub, dominance);
        }
    }

    @DisplayName("Golomb ruler")
    @TestFactory
    public Stream<DynamicTest> testGR() {
        var bench = new GRBench();
        return bench.generateTests();
    }
}
