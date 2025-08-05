package org.ddolib.examples.astar.knapsack;

import org.ddolib.astar.core.solver.AStarSolver;
import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.common.solver.Solver;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.ddo.core.heuristics.variable.VariableHeuristic;
import org.ddolib.examples.ddo.knapsack.*;
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

public class KSTest {
    private static class KSAStartBench extends ProblemTestBench<Integer, Integer, KSProblem> {

        public KSAStartBench() {
            super(false, false, true);
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
            VariableHeuristic<Integer> varh = new DefaultVariableHeuristic<>();
            SimpleFrontier<Integer> frontier = new SimpleFrontier<>(ranking, CutSetType.LastExactLayer);
            SimpleDominanceChecker<Integer, Integer> dominanceChecker =
                    new SimpleDominanceChecker<>(new KSDominance(), problem.nbVars());

            return new SolverConfig<>(relax, varh, ranking, 2, 20, frontier, fub, dominanceChecker);
        }

        @Override
        protected <U> Solver solverForTests(SolverConfig<Integer, U> config, KSProblem problem) {
            KSFastUpperBound fub = new KSFastUpperBound(problem);
            return new AStarSolver<>(problem, config.varh(), fub, config.dominance());
        }
    }

    @DisplayName("Knapsack with A*")
    @TestFactory
    public Stream<DynamicTest> testKSWithAStar() {
        var bench = new KSAStartBench();
        return bench.generateTests();
    }
}
