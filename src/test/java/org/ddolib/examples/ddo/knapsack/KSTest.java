package org.ddolib.examples.ddo.knapsack;

import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.ddo.core.heuristics.variable.VariableHeuristic;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
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

public class KSTest {
    private static class KSBench extends ProblemTestBench<Integer, Integer, KSProblem> {

        /**
         * Instantiate a test bench.
         *
         * @param testRelaxation Whether the relaxation must be tested.
         * @param testFUB        Whether the fast upper bound must be tested.
         * @param testDominance  Whether the dominance must be tested.
         */
        public KSBench(boolean testRelaxation, boolean testFUB, boolean testDominance) {
            super(testRelaxation, testFUB, testDominance);
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
            KSRelax relax = new KSRelax();
            KSFastUpperBound fub = new KSFastUpperBound(problem);
            KSRanking ranking = new KSRanking();
            FixedWidth<Integer> width = new FixedWidth<>(1000);
            VariableHeuristic<Integer> varh = new DefaultVariableHeuristic<>();
            SimpleFrontier<Integer> frontier = new SimpleFrontier<>(ranking, CutSetType.LastExactLayer);
            SimpleDominanceChecker<Integer, Integer> dominanceChecker =
                    new SimpleDominanceChecker<>(new KSDominance(), problem.nbVars());

            return new SolverConfig<>(relax, varh, ranking, 2, 20, frontier, fub,
                    dominanceChecker, SolverType.EXACT);
        }
    }

    @DisplayName("Knapsack")
    @TestFactory
    public Stream<DynamicTest> testKS() {
        var bench = new KSBench(true, true, true);
        return bench.generateTests();
    }
}
