package org.ddolib.examples.ddo.tstptw;

import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.Frontier;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.ddo.core.heuristics.variable.VariableHeuristic;
import org.ddolib.examples.ddo.tsptw.*;
import org.ddolib.util.testbench.ProblemTestBench;
import org.ddolib.util.testbench.SolverConfig;
import org.ddolib.util.testbench.SolverType;
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

        /**
         * Instantiate a test bench.
         *
         * @param testRelaxation Whether the relaxation must be tested.
         * @param testFUB        Whether the fast upper bound must be tested.
         * @param testDominance  Whether the dominance must be tested.
         */
        public TSPTWBench(boolean testRelaxation, boolean testFUB, boolean testDominance) {
            super(testRelaxation, testFUB, testDominance);
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
            TSPTWRelax relax = new TSPTWRelax(problem);
            TSPTWRanking ranking = new TSPTWRanking();
            TSPTWFastUpperBound fub = new TSPTWFastUpperBound(problem);

            VariableHeuristic<TSPTWState> varh = new DefaultVariableHeuristic<>();
            SimpleDominanceChecker<TSPTWState, TSPTWDominanceKey> dominance =
                    new SimpleDominanceChecker<>(new TSPTWDominance(), problem.nbVars());
            Frontier<TSPTWState> frontier = new SimpleFrontier<>(ranking, CutSetType.LastExactLayer);

            return new SolverConfig<>(relax, varh, ranking, 2, 20, frontier, fub, dominance, SolverType.EXACT);
        }
    }

    @DisplayName("TSPTW")
    @TestFactory
    public Stream<DynamicTest> testTSPTW() {
        var bench = new TSPTWBench(true, true, true);
        return bench.generateTests();
    }
}
