package org.ddolib.examples.knapsack;

import org.ddolib.astar.core.solver.AStarSolver;
import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.common.solver.Solver;
import org.ddolib.common.solver.SolverConfig;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.cluster.*;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.solver.SequentialSolver;
import org.ddolib.util.testbench.ProblemTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.ddolib.examples.knapsack.KSMain.readInstance;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class KSTest {
    private static class KSBench extends ProblemTestBench<Integer, Integer, KSProblem> {

        public KSBench() {
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
            config.relax = new KSRelax();
            config.ranking = new KSRanking();
            config.width = new FixedWidth<>(10);
            config.varh = new DefaultVariableHeuristic<>();
            config.restrictStrategy = new CostBased<>(config.ranking);
            config.relaxStrategy = new CostBased<>(config.ranking);
            config.fub = new KSFastUpperBound(problem);
            config.dominance = new SimpleDominanceChecker<>(new KSDominance(), problem.nbVars());
            config.frontier = new SimpleFrontier<>(config.ranking, CutSetType.LastExactLayer);

            return config;
        }
    }

    @DisplayName("Knapsack")
    @TestFactory
    public Stream<DynamicTest> testKS() {
        var bench = new KSBench();
        bench.testRelaxation = true;
        bench.testFUB = true;
        bench.testDominance = true;
        bench.testCache = true;
        return bench.generateTests();
    }

    static Stream<KSProblem> dataProvider() {
        Stream<Integer> testStream = IntStream.rangeClosed(0, 9).boxed();
        return testStream.flatMap(i -> {
            try {
                return Stream.of(readInstance("src/test/resources/Knapsack/instance_test_" + i));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testKnapsack(KSProblem problem) {
        SolverConfig<Integer, Integer> config = new SolverConfig<>();
        config.problem = problem;
        config.relax = new KSRelax();
        config.ranking = new KSRanking();
        config.width = new FixedWidth<>(10);
        config.varh = new DefaultVariableHeuristic<>();
        config.fub = new KSFastUpperBound(problem);
        config.frontier = new SimpleFrontier<>(config.ranking, CutSetType.LastExactLayer);
        config.relaxStrategy = new CostBased<>(config.ranking);
        config.restrictStrategy = new CostBased<>(config.ranking);

        final Solver solver = new SequentialSolver<>(config);

        solver.maximize();
        assertEquals(solver.bestValue().get(), problem.optimalValue().get());
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testKnapsackGHP(KSProblem problem) {
        SolverConfig<Integer, Integer> config = new SolverConfig<>();
        config.problem = problem;
        config.relax = new KSRelax();
        config.ranking = new KSRanking();
        config.width = new FixedWidth<>(10);
        config.varh = new DefaultVariableHeuristic<>();
        config.fub = new KSFastUpperBound(problem);
        config.frontier = new SimpleFrontier<>(config.ranking, CutSetType.LastExactLayer);
        config.relaxStrategy = new GHP<>(new KSDistance());
        config.restrictStrategy = config.relaxStrategy;

        final Solver solver = new SequentialSolver<>(config);

        solver.maximize();
        assertEquals(solver.bestValue().get(), problem.optimalValue().get());
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testKnapsackKmeans(KSProblem problem) {
        SolverConfig<Integer, Integer> config = new SolverConfig<>();
        config.problem = problem;
        config.relax = new KSRelax();
        config.ranking = new KSRanking();
        config.width = new FixedWidth<>(10);
        config.varh = new DefaultVariableHeuristic<>();
        config.fub = new KSFastUpperBound(problem);
        config.frontier = new SimpleFrontier<>(config.ranking, CutSetType.LastExactLayer);
        config.relaxStrategy = new Kmeans<>(new KSCoordinates());
        config.restrictStrategy = config.relaxStrategy;

        final Solver solver = new SequentialSolver<>(config);

        solver.maximize();
        assertEquals(solver.bestValue().get(), problem.optimalValue().get());
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void fastUpperBoundTest(KSProblem problem) throws IOException {
        final KSFastUpperBound fub = new KSFastUpperBound(problem);

        HashSet<Integer> vars = new HashSet<>();
        for (int i = 0; i < problem.nbVars(); i++) {
            vars.add(i);
        }

        double rub = fub.fastUpperBound(problem.capa, vars);
        // Checks if the upper bound at the root is bigger than the optimal solution
        assertTrue(rub >= problem.optimalValue().get(),
                String.format("Upper bound %.1f is not bigger than the expected optimal solution %.1f",
                        rub,
                        problem.optimalValue().get()));
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testKnapsackWithDominance(KSProblem problem) {
        SolverConfig<Integer, Integer> config = new SolverConfig<>();
        config.problem = problem;
        config.relax = new KSRelax();
        config.ranking = new KSRanking();
        config.width = new FixedWidth<>(10);
        config.varh = new DefaultVariableHeuristic<>();
        config.fub = new KSFastUpperBound(problem);
        config.dominance = new SimpleDominanceChecker<>(new KSDominance(), problem.nbVars());
        config.frontier = new SimpleFrontier<>(config.ranking, CutSetType.LastExactLayer);
        final Solver solver = new SequentialSolver<>(config);

        solver.maximize();
        assertEquals(solver.bestValue().get(), problem.optimalValue().get());
    }


    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testAstar(KSProblem problem) {
        SolverConfig<Integer, Integer> config = new SolverConfig<>();
        config.problem = problem;
        config.varh = new DefaultVariableHeuristic<>();
        config.fub = new KSFastUpperBound(problem);
        config.dominance = new SimpleDominanceChecker<>(new KSDominance(), problem.nbVars());

        final Solver solver = new AStarSolver<>(config);

        solver.maximize();
        assertEquals(solver.bestValue().get(), problem.optimalValue().get());
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testRelaxStrats(KSProblem problem) {
        SolverConfig<Integer, Integer> config = new SolverConfig<>();
        config.problem = problem;
        config.ranking = new KSRanking();
        config.width = new FixedWidth<>(5);
        config.fub = new KSFastUpperBound(problem);
        config.relax = new KSRelax();
        ReductionStrategy[] strats = new ReductionStrategy[5];
        strats[0] = new Kmeans<>(new KSCoordinates());
        strats[1] = new CostBased<>(new KSRanking());
        strats[2] = new CostUBBased<>(new KSRanking());
        strats[3] = new GHP<>(new KSDistance());
        strats[4] = new MBP<>(new KSDistance());

        for (ReductionStrategy strategy : strats) {
            config.dominance = new SimpleDominanceChecker<>(new KSDominance(), problem.nbVars());
            config.frontier = new SimpleFrontier<>(config.ranking, CutSetType.LastExactLayer);
            config.varh = new DefaultVariableHeuristic<>();
            config.relaxStrategy = strategy;

            Solver solver = new SequentialSolver<>(config);
            solver.maximize();
            assertEquals(solver.bestValue().get(), problem.optimalValue().get());
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testRestrictStrats(KSProblem problem) {
        SolverConfig<Integer, Integer> config = new SolverConfig<>();
        config.problem = problem;
        config.ranking = new KSRanking();
        config.width = new FixedWidth<>(5);
        config.fub = new KSFastUpperBound(problem);
        config.relax = new KSRelax();
        ReductionStrategy[] strats = new ReductionStrategy[5];
        strats[0] = new Kmeans<>(new KSCoordinates());
        strats[1] = new CostBased<>(new KSRanking());
        strats[2] = new CostUBBased<>(new KSRanking());
        strats[3] = new GHP<>(new KSDistance());
        strats[4] = new MBP<>(new KSDistance());

        for (ReductionStrategy strategy : strats) {
            config.dominance = new SimpleDominanceChecker<>(new KSDominance(), problem.nbVars());
            config.frontier = new SimpleFrontier<>(config.ranking, CutSetType.LastExactLayer);
            config.varh = new DefaultVariableHeuristic<>();
            config.restrictStrategy = strategy;

            Solver solver = new SequentialSolver<>(config);
            solver.maximize();
            assertEquals(solver.bestValue().get(), problem.optimalValue().get());
        }
    }
}
