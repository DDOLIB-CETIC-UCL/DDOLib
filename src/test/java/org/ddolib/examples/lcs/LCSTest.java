package org.ddolib.examples.lcs;

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

public class LCSTest {
    private static class LCSBench extends ProblemTestBench<LCSState, LCSProblem> {

        public LCSBench() {
            super();
        }

        @Override
        protected List<LCSProblem> generateProblems() {
            String dir = Paths.get("src", "test", "resources", "LCS").toString();

            File[] files = new File(dir).listFiles();
            assert files != null;
            Stream<File> stream = Stream.of(files);

            return stream.filter(file -> !file.isDirectory())
                    .map(File::getName)
                    .map(fileName -> Paths.get(dir, fileName))
                    .map(filePath -> {
                        try {
                            LCSProblem problem = LCSMain.extractFile(filePath.toString());
                            problem.setName(filePath.getFileName().toString());
                            return problem;
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }).toList();
        }

        @Override
        protected SolverConfig<LCSState> configSolver(LCSProblem problem) {
            SolverConfig<LCSState> config = new SolverConfig<>();
            config.problem = problem;
            config.relax = new LCSRelax(problem);
            config.ranking = new LCSRanking();
            config.flb = new LCSFastLowerBound(problem);

            config.width = new FixedWidth<>(maxWidth);
            config.varh = new DefaultVariableHeuristic<>();
            config.frontier = new SimpleFrontier<>(config.ranking, CutSetType.LastExactLayer);
            return config;
        }
    }

    @DisplayName("LCS")
    @TestFactory
    public Stream<DynamicTest> testLCS() {
        var bench = new LCSBench();
        bench.testRelaxation = true;
        bench.testFLB = true;
        return bench.generateTests();
    }


}
