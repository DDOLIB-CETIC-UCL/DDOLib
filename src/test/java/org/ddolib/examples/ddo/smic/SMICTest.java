package org.ddolib.examples.ddo.smic;

import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.common.solver.Solver;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.Frontier;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.ddo.core.heuristics.variable.VariableHeuristic;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.util.testbench.ProblemTestBench;
import org.ddolib.ddo.util.testbench.SolverConfig;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;
public class SMICTest {

    private static class SMICBench extends ProblemTestBench<SMICState, Integer, SMICProblem> {

        /**
         * Instantiate a test bench.
         *
         * @param testRelaxation Whether the relaxation must be tested.
         * @param testFUB        Whether the fast upper bound must be tested.
         * @param testDominance  Whether the dominance must be tested.
         */
        public SMICBench(boolean testRelaxation, boolean testFUB, boolean testDominance) {
            super(testRelaxation, testFUB, testDominance);
        }

        @Override
        protected List<SMICProblem> generateProblems() {
            String dir = Paths.get("src", "test", "resources", "SMIC").toString();

            File[] files = new File(dir).listFiles();
            assert files != null;
            Stream<File> stream = Stream.of(files);

            return stream.filter(file -> !file.isDirectory())
                    .map(File::getName)
                    .map(fileName -> Paths.get(dir, fileName))
                    .map(filePath -> {
                        try {
                            SMICProblem problem = SMICMain.readProblem(filePath.toString());
                            return problem;
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }).toList();
        }

        @Override
        protected SolverConfig<SMICState, Integer> configSolver(SMICProblem problem) {
            SMICRelax relax = new SMICRelax(problem);
            SMICRanking ranking = new SMICRanking();
            FixedWidth<SMICState> width = new FixedWidth<>(2);
            VariableHeuristic<SMICState> varh = new DefaultVariableHeuristic<SMICState>();
            SimpleDominanceChecker<SMICState, Integer> dominance = new SimpleDominanceChecker<>(
                    new SMICDominance(), problem.nbVars());
            Frontier<SMICState> frontier = new SimpleFrontier<>(ranking, CutSetType.LastExactLayer);

            return new SolverConfig<>(relax, varh, ranking, width, frontier, dominance);
        }
    }

    /*@DisplayName("SMIC")
    @TestFactory
    public Stream<DynamicTest> testSMIC() {
        var bench = new SMICBench(false, false, true);
        return bench.generateTests();
    }*/
}
