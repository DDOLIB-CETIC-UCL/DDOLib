package org.ddolib.examples.tstptw;

import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.common.solver.SolverConfig;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.util.testbench.ProblemTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

public class TSPTWTests {

    private static class TSPTWBench extends ProblemTestBench<TSPTWState, TSPTWDominanceKey, TSPTWProblem> {

        public TSPTWBench() {
            super();
        }

        @Override
        protected List<TSPTWProblem> generateProblems() {
            String dir = Paths.get("src", "test", "resources", "TSPTW").toString();

            File[] files = new File(dir).listFiles();
            assert files != null;
            Stream<File> stream = Stream.of(files);
            return stream.filter(file -> !file.isDirectory())
                    .map(File::getName)
                    .map(fileName -> Paths.get(dir, fileName))
                    .map(filePath -> {
                        try {
                            TSPTWProblem problem = new TSPTWProblem(new TSPTWInstance(filePath.toString()));
                            problem.setName(filePath.getFileName().toString());
                            return problem;
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }).toList();
        }

        @Override
        protected SolverConfig<TSPTWState, TSPTWDominanceKey> configSolver(TSPTWProblem problem) {
            SolverConfig<TSPTWState, TSPTWDominanceKey> config = new SolverConfig<>();
            config.problem = problem;
            config.relax = new TSPTWRelax(problem);
            config.ranking = new TSPTWRanking();
            config.fub = new TSPTWFastUpperBound(problem);

            config.width = new FixedWidth<>(20);
            config.varh = new DefaultVariableHeuristic<>();
            config.dominance = new SimpleDominanceChecker<>(new TSPTWDominance(), problem.nbVars());
            config.frontier = new SimpleFrontier<>(config.ranking, CutSetType.LastExactLayer);
            return config;
        }
    }

    @DisplayName("TSPTW")
    @TestFactory
    public Stream<DynamicTest> testTSPTW() {
        var bench = new TSPTWBench();
        bench.testRelaxation = true;
        bench.testFUB = true;
        bench.testDominance = true;
        bench.testCache = true;
        return bench.generateTests();
    }
}
