package org.ddolib.examples.ddo.tsp;

import org.ddolib.common.dominance.DefaultDominanceChecker;
import org.ddolib.common.solver.Solver;
import org.ddolib.ddo.core.frontier.CutSetType;
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
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TSPTests {

    private static class TSPBench extends ProblemTestBench<TSPState, Integer, TSPProblem> {

        public TSPBench() {
            super(true, true, false);
        }

        @Override
        protected List<TSPProblem> generateProblems() {
            String dir = Paths.get("src", "test", "resources", "TSP").toString();
            File[] files = new File(dir).listFiles();
            assert files != null;
            Stream<File> stream = Stream.of(files);

            return stream.filter(file -> !file.isDirectory())
                    .map(File::getName)
                    .map(fileName -> Paths.get(dir, fileName))
                    .map(filePath -> {
                        TSPInstance instance = new TSPInstance(filePath.toString());
                        TSPProblem problem = new TSPProblem(instance.distanceMatrix, instance.objective);
                        problem.setName(filePath.getFileName().toString());
                        return problem;
                    }).toList();
        }

        @Override
        protected SolverConfig<TSPState, Integer> configSolver(TSPProblem problem) {
            TSPRelax relax = new TSPRelax(problem);
            TSPRanking ranking = new TSPRanking();
            TSPFastUpperBound fub = new TSPFastUpperBound(problem);
            VariableHeuristic<TSPState> varh = new DefaultVariableHeuristic<>();
            SimpleFrontier<TSPState> frontier = new SimpleFrontier<>(ranking, CutSetType.LastExactLayer);
            DefaultDominanceChecker<TSPState> dominanceChecker = new DefaultDominanceChecker<>();

            return new SolverConfig<>(relax, varh, ranking, 2, 20, frontier, fub, dominanceChecker);
        }
    }

    @DisplayName("TSP")
    @TestFactory
    public Stream<DynamicTest> testTSP() {
        var bench = new TSPBench();
        return bench.generateTests();
    }

    static Stream<TSPInstance> dataProvider() throws IOException {
        String dir = Paths.get("src", "test", "resources", "TSP").toString();

        File[] files = new File(dir).listFiles();
        assert files != null;
        Stream<File> stream = Stream.of(files);
        return stream.filter(file -> !file.isDirectory())
                .map(File::getName)
                .map(fileName -> Paths.get(dir, fileName))
                .map(filePath -> new TSPInstance(filePath.toString()));
    }

    static Stream<TSPInstance> dataProvider2() throws IOException {
        return IntStream.range(0, 100).boxed().map(i ->
                new TSPInstance(3 + i % 10, i, 1000));
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testTSP(TSPInstance instance) {

        Solver s = TSPMain.solveTSP(instance);
        TSPProblem problem = new TSPProblem(instance.distanceMatrix);
        int[] solution = TSPMain.extractSolution(problem, s);
        assertEquals(s.bestValue().get(), -problem.eval(solution));
        if (instance.objective >= 0) {
            System.out.println("comparing obj with actual best");
            assertEquals(instance.objective, -s.bestValue().get());
        }
    }
}
