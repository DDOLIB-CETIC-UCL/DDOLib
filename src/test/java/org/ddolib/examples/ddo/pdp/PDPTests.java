package org.ddolib.examples.ddo.pdp;

import org.ddolib.common.dominance.DefaultDominanceChecker;
import org.ddolib.common.solver.Solver;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.Frontier;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.ddo.core.heuristics.variable.VariableHeuristic;
import org.ddolib.util.testbench.ProblemTestBench;
import org.ddolib.util.testbench.SolverConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PDPTests {

    private static class PDPBench extends ProblemTestBench<PDPState, Integer, PDPProblem> {

        public PDPBench() {
            super(true, true, false);
        }

        @Override
        protected List<PDPProblem> generateProblems() {
            String dir = Paths.get("src", "test", "resources", "PDP").toString();

            File[] files = new File(dir).listFiles();
            assert files != null;
            Stream<File> stream = Stream.of(files);

            return stream.filter(file -> !file.isDirectory())
                    .map(File::getName)
                    .map(fileName -> Paths.get(dir, fileName))
                    .map(filePath -> {
                        try {
                            PDPInstance instance = new PDPInstance(filePath.toString());
                            PDPProblem problem = new PDPProblem(instance);
                            problem.setName(filePath.getFileName().toString());
                            return problem;
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }).toList();
        }

        @Override
        protected SolverConfig<PDPState, Integer> configSolver(PDPProblem problem) {
            PDPRelax relax = new PDPRelax(problem);
            PDPRanking ranking = new PDPRanking();
            PDPFastUpperBound fub = new PDPFastUpperBound(problem);
            VariableHeuristic<PDPState> varh = new DefaultVariableHeuristic<>();
            Frontier<PDPState> frontier = new SimpleFrontier<>(ranking, CutSetType.LastExactLayer);
            DefaultDominanceChecker<PDPState> dominanceChecker = new DefaultDominanceChecker<>();

            return new SolverConfig<>(relax, varh, ranking, 2, 20, frontier, fub, dominanceChecker);
        }
    }

    @DisplayName("PDP")
    @TestFactory
    public Stream<DynamicTest> testPDP() {
        var bench = new PDPBench();
        return bench.generateTests();
    }

    static Stream<PDPInstance> dataProvider2() {
        return IntStream.range(0, 10).boxed().map(i ->
                PDPMain.genInstance(5 + i % 14, i % 3, new Random(i)));
    }

    @ParameterizedTest
    @MethodSource("dataProvider2")
    public void testPDP(PDPInstance instance) {

        PDPProblem problem = new PDPProblem(instance);
        Solver s = PDPMain.solveDPD(problem);

        PDPSolution solution = PDPMain.extractSolution(s, problem);

        assertEquals(solution.value, instance.eval(solution.solution));
    }
}
