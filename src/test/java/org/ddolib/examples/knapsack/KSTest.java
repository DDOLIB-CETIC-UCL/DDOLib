package org.ddolib.examples.knapsack;

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

public class KSTest {
    private static class KSBench extends ProblemTestBench<Integer, Integer, KSProblem> {

        public KSBench() {
            super();
        }

        @Override
        protected List<KSProblem> generateProblems() {
            String dir = Paths.get("src", "test", "resources", "Knapsack").toString();

            File[] files = new File(dir).listFiles();
            assert files != null;
            Stream<File> stream = Stream.of(files);

            return stream.filter(file -> !file.isDirectory())
                    .map(File::getName)
                    .map(fileName -> Paths.get(dir, fileName))
                    .map(filePath -> {
                        try {
                            KSProblem problem = KSMain.readInstance(filePath.toString());
                            problem.setName(filePath.getFileName().toString());
                            return problem;
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }).toList();
        }

        @Override
        protected SolverConfig<Integer, Integer> configSolver(KSProblem problem) {
            SolverConfig<Integer, Integer> config = new SolverConfig<>();
            config.problem = problem;
            config.relax = new KSRelax();
            config.ranking = new KSRanking();
            config.width = new FixedWidth<>(10);
            config.varh = new DefaultVariableHeuristic<>();
            config.fub = new KSFastUpperBound(problem);
            config.dominance = new SimpleDominanceChecker<>(new KSDominance(), problem.nbVars());
            config.frontier = new SimpleFrontier<>(config.ranking, CutSetType.LastExactLayer);

            return config;
        }
    }

    @DisplayName("Knapsack")
    @TestFactory
    public Stream<DynamicTest> testKS() {
        var bench = new KSBench();
        bench.testRelaxation = true;
        bench.testFUB = true;
        bench.testDominance = true;
        bench.testCache = true;
        return bench.generateTests();
    }
}
