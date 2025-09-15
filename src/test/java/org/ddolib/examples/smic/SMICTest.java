package org.ddolib.examples.smic;

import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.common.solver.Solver;
import org.ddolib.common.solver.SolverConfig;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.cluster.CostBased;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.solver.SequentialSolver;
import org.ddolib.util.testbench.ProblemTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

public class SMICTest {

    private static class SMICBench extends ProblemTestBench<SMICState, Integer, SMICProblem> {

        public SMICBench() {
            super();
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
                            return SMICMain.readProblem(filePath.toString());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }).toList();
        }

        @Override
        protected SolverConfig<SMICState, Integer> configSolver(SMICProblem problem) {
            SolverConfig<SMICState, Integer> config = new SolverConfig<>();
            config.problem = problem;
            config.relax = new SMICRelax(problem);
            config.ranking = new SMICRanking();
            config.width = new FixedWidth<>(maxWidth);
            config.varh = new DefaultVariableHeuristic<>();
            config.dominance = new SimpleDominanceChecker<>(new SMICDominance(), problem.nbVars());
            config.frontier = new SimpleFrontier<>(config.ranking, CutSetType.LastExactLayer);
            config.restrictStrategy = new CostBased<>(config.ranking);
            config.relaxStrategy = new CostBased<>(config.ranking);
            return config;
        }

        @Override
        protected Solver solverForTests(SolverConfig<SMICState, Integer> config) {
            config.width = new FixedWidth<>(100);
            return new SequentialSolver<>(config);
        }
    }

    @DisplayName("SMIC")
    @TestFactory
    public Stream<DynamicTest> testSMIC() {
        var bench = new SMICBench();
        bench.testRelaxation = true;
        bench.testDominance = true;
        return bench.generateTests();
    }
}
