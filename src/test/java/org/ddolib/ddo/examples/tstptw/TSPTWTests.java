package org.ddolib.ddo.examples.tstptw;

import org.ddolib.ddo.core.CutSetType;
import org.ddolib.ddo.core.Frontier;
import org.ddolib.ddo.examples.tsptw.*;
import org.ddolib.ddo.heuristics.VariableHeuristic;
import org.ddolib.ddo.implem.dominance.SimpleDominanceChecker;
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
            FixedWidth<TSPTWState> width = new FixedWidth<>(2);
            VariableHeuristic<TSPTWState> varh = new DefaultVariableHeuristic<>();
            SimpleDominanceChecker<TSPTWState, TSPTWDominanceKey> dominance =
                    new SimpleDominanceChecker<>(new TSPTWDominance(), problem.nbVars());
            Frontier<TSPTWState> frontier = new SimpleFrontier<>(ranking, CutSetType.LastExactLayer);

            return new SolverConfig<>(relax, varh, ranking, width, frontier, dominance);
        }
    }

    @DisplayName("TSPTW")
    @TestFactory
    public Stream<DynamicTest> testTSPTW() throws IOException {
        var bench = new TSPTWBench(true, true, true);
        return bench.generateTests();
    }
}
