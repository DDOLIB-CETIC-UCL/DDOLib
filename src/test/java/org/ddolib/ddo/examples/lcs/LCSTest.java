package org.ddolib.ddo.examples.lcs;

import org.ddolib.ddo.core.CutSetType;
import org.ddolib.ddo.core.Frontier;
import org.ddolib.ddo.heuristics.VariableHeuristic;
import org.ddolib.ddo.implem.dominance.DefaultDominanceChecker;
import org.ddolib.ddo.implem.frontier.SimpleFrontier;
import org.ddolib.ddo.implem.heuristics.DefaultVariableHeuristic;
import org.ddolib.ddo.implem.heuristics.FixedWidth;
import org.ddolib.ddo.util.testbench.ProblemTestBench;
import org.ddolib.ddo.util.testbench.SolverConfig;
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
        /**
         * Instantiate a test bench.
         *
         * @param testRelaxation Whether the relaxation must be tested.
         * @param testFUB        Whether the fast upper bound must be tested.
         * @param testDominance  Whether the dominance must be tested.
         */
        public LCSBench(boolean testRelaxation, boolean testFUB, boolean testDominance) {
            super(testRelaxation, testFUB, testDominance);
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

            FixedWidth<LCSState> width = new FixedWidth<>(1000);
            VariableHeuristic<LCSState> varh = new DefaultVariableHeuristic<>();
            Frontier<LCSState> frontier = new SimpleFrontier<>(ranking, CutSetType.LastExactLayer);
            DefaultDominanceChecker<LCSState> dominanceChecker = new DefaultDominanceChecker<>();
            return new SolverConfig<>(relax, varh, ranking, width, frontier, dominanceChecker);
        }
    }

    @DisplayName("LCS")
    @TestFactory
    public Stream<DynamicTest> testLCS() {
        var bench = new LCSBench(true, true, false);
        return bench.generateTests();
    }


}
