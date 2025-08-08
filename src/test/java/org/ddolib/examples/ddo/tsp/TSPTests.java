package org.ddolib.examples.ddo.tsp;

import org.ddolib.common.dominance.DefaultDominanceChecker;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.ddo.core.heuristics.variable.VariableHeuristic;
import org.ddolib.util.testbench.ProblemTestBench;
import org.ddolib.util.testbench.SolverConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

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
}
