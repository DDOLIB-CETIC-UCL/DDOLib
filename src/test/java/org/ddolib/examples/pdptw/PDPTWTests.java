package org.ddolib.examples.pdptw;

import org.ddolib.common.solver.SolverConfig;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.util.testbench.ProblemTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import javax.lang.model.type.NullType;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.ddolib.examples.pdptw.PDPTWMain.genInstance3;

public class PDPTWTests {

    private static class PDPTWBench extends ProblemTestBench<PDPTWState, NullType, PDPTWProblem> {

        public PDPTWBench() {
            super();
        }

        @Override
        protected List<PDPTWProblem> generateProblems() {
            Random random = new Random(1);
            int nbTests = 10;

            return IntStream.range(0, nbTests).boxed().map(
                     i -> new PDPTWProblem(genInstance3(8, 1, 2, random))
            ).toList();
        }

        @Override
        protected SolverConfig<PDPTWState, NullType> configSolver(PDPTWProblem problem) {
            SolverConfig<PDPTWState, NullType> config = new SolverConfig<>();
            config.problem = problem;
            config.relax = new PDPTWRelax(problem);
            config.ranking = new PDPTWRanking();
            config.flb = new PDPTWFastLowerBound(problem);
            config.width = new FixedWidth<>(maxWidth);
            config.varh = new DefaultVariableHeuristic<>();
            config.frontier = new SimpleFrontier<>(config.ranking, CutSetType.Frontier);

            return config;
        }
    }

    @DisplayName("PDPTW")
    @TestFactory
    public Stream<DynamicTest> testPDPTW() {
        var bench = new PDPTWBench();
        bench.testRelaxation = true;
        bench.testFLB = true;
        bench.minWidth = 45;
        bench.maxWidth = 50;
        return bench.generateTests();
    }
}
