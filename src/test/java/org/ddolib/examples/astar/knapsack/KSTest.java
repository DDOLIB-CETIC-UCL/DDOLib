package org.ddolib.examples.astar.knapsack;

import org.ddolib.astar.core.solver.AStarSolver;
import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.common.solver.Solver;
import org.ddolib.common.solver.SolverConfig;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.examples.ddo.knapsack.KSDominance;
import org.ddolib.examples.ddo.knapsack.KSFastUpperBound;
import org.ddolib.examples.ddo.knapsack.KSMain;
import org.ddolib.examples.ddo.knapsack.KSProblem;
import org.ddolib.util.testbench.ProblemTestBench;
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
            super();
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
            SolverConfig<Integer, Integer> config = new SolverConfig<>();
            config.problem = problem;
            config.fub = new KSFastUpperBound(problem);
            config.varh = new DefaultVariableHeuristic<>();
            config.dominance = new SimpleDominanceChecker<>(new KSDominance(), problem.nbVars());

            return config;
        }

        @Override
        protected Solver solverForTests(SolverConfig<Integer, Integer> config) {
            config.fub = new KSFastUpperBound((KSProblem) config.problem);
            return new AStarSolver<>(config);
        }
    }

    @DisplayName("Knapsack with A*")
    @TestFactory
    public Stream<DynamicTest> testKSWithAStar() {
        var bench = new KSAStartBench();
        bench.testRelaxation = false; //Not need for A*
        bench.testFUB = false; // Already tested in the model
        bench.testDominance = true;
        return bench.generateTests();
    }
}
