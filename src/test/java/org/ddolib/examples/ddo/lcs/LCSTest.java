package org.ddolib.examples.ddo.lcs;

import org.ddolib.common.dominance.DefaultDominanceChecker;
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

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

public class LCSTest {
    private static class LCSBench extends ProblemTestBench<LCSState, Integer, LCSProblem> {

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
        protected SolverConfig<LCSState, Integer> configSolver(LCSProblem problem) {
            LCSRelax relax = new LCSRelax(problem);
            LCSRanking ranking = new LCSRanking();
            LCSFastUpperBound fub = new LCSFastUpperBound(problem);

            VariableHeuristic<LCSState> varh = new DefaultVariableHeuristic<>();
            Frontier<LCSState> frontier = new SimpleFrontier<>(ranking, CutSetType.LastExactLayer);
            DefaultDominanceChecker<LCSState> dominanceChecker = new DefaultDominanceChecker<>();
            return new SolverConfig<>(relax, varh, ranking, 2, 20, frontier, fub, dominanceChecker);
        }
    }

    @DisplayName("LCS")
    @TestFactory
    public Stream<DynamicTest> testLCS() {
        var bench = new LCSBench();
        bench.testRelaxation = true;
        bench.testFUB = true;
        return bench.generateTests();
    }


}
