package org.ddolib.ddo.examples.pigmentscheduling;

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
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

class PSTest {

    private static class PSPBench extends ProblemTestBench<PSState, Integer, PSProblem> {

        /**
         * Instantiate a test bench.
         *
         * @param testRelaxation Whether the relaxation must be tested.
         * @param testFUB        Whether the fast upper bound must be tested.
         * @param testDominance  Whether the dominance must be tested.
         */
        public PSPBench(boolean testRelaxation, boolean testFUB, boolean testDominance) {
            super(testRelaxation, testFUB, testDominance);
        }

        @Override
        protected List<PSProblem> generateProblems() {
            String dir = Paths.get("src", "test", "resources", "PSP", "2items").toString();

            File[] files = new File(dir).listFiles();
            assert files != null;
            Stream<File> stream = Stream.of(files);

            return stream.filter(file -> !file.isDirectory())
                    .map(File::getName)
                    .map(fileName -> Paths.get(dir, fileName))
                    .map(filePath -> {
                        PSProblem problem = new PSProblem(new PSInstance(filePath.toString()));
                        problem.setName(filePath.getFileName().toString());
                        return problem;
                    }).toList();
        }

        @Override
        protected SolverConfig<PSState, Integer> configSolver(PSProblem problem) {
            PSRelax relax = new PSRelax(problem.instance);
            PSRanking ranking = new PSRanking();
            FixedWidth<PSState> width = new FixedWidth<>(10);
            VariableHeuristic<PSState> varh = new DefaultVariableHeuristic<>();
            Frontier<PSState> frontier = new SimpleFrontier<>(ranking, CutSetType.LastExactLayer);
            DefaultDominanceChecker<PSState> dominanceChecker = new DefaultDominanceChecker<>();

            return new SolverConfig<>(relax, varh, ranking, width, frontier, dominanceChecker);
        }
    }

    @DisplayName("PSP")
    @TestFactory
    public Stream<DynamicTest> testPSP() {
        var bench = new PSPBench(true, true, false);
        return bench.generateTests();
    }
}