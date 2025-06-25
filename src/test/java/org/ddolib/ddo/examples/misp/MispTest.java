package org.ddolib.ddo.examples.misp;

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
import java.util.BitSet;
import java.util.List;
import java.util.stream.Stream;

public class MispTest {

    private static class MispBench extends ProblemTestBench<BitSet, Integer, MispProblem> {

        /**
         * Instantiate a test bench.
         *
         * @param testRelaxation Whether the relaxation must be tested.
         * @param testFUB        Whether the fast upper bound must be tested.
         * @param testDominance  Whether the dominance must be tested.
         */
        public MispBench(boolean testRelaxation, boolean testFUB, boolean testDominance) {
            super(testRelaxation, testFUB, testDominance);
        }

        @Override
        protected List<MispProblem> generateProblems() {
            String dir = Paths.get("src", "test", "resources", "MISP").toString();
            File[] files = new File(dir).listFiles();
            assert files != null;
            Stream<File> stream = Stream.of(files);
            return stream.filter(file -> !file.isDirectory())
                    .map(File::getName)
                    .map(fileName -> Paths.get(dir, fileName))
                    .map(filePath -> {
                        try {
                            MispProblem problem = MispMain.readFile(filePath.toString());
                            problem.setName(filePath.getFileName().toString());
                            return problem;
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }).toList();
        }

        @Override
        protected SolverConfig<BitSet, Integer> configSolver(MispProblem problem) {
            MispRelax relax = new MispRelax(problem);
            MispRanking ranking = new MispRanking();
            FixedWidth<BitSet> width = new FixedWidth<>(1000);
            VariableHeuristic<BitSet> varh = new DefaultVariableHeuristic<>();
            Frontier<BitSet> frontier = new SimpleFrontier<>(ranking, CutSetType.LastExactLayer);
            DefaultDominanceChecker<BitSet> dominanceChecker = new DefaultDominanceChecker<>();
            return new SolverConfig<>(relax, varh, ranking, width, frontier, dominanceChecker);
        }
    }

    @DisplayName("MISP")
    @TestFactory
    public Stream<DynamicTest> testMISP() {
        var bench = new MispBench(true, true, false);
        return bench.generateTests();
    }


}
