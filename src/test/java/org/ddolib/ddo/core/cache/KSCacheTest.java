package org.ddolib.ddo.core.cache;

import org.ddolib.common.solver.Solver;
import org.ddolib.common.solver.SolverConfig;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.solver.SequentialSolver;
import org.ddolib.examples.knapsack.KSFastLowerBound;
import org.ddolib.examples.knapsack.KSProblem;
import org.ddolib.examples.knapsack.KSRanking;
import org.ddolib.examples.knapsack.KSRelax;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class KSCacheTest {

    static Stream<KSProblem> dataProvider() throws IOException {
        Random rand = new Random(10);
        int number = 1000;
        int nbVars = 10;
        int cap = 10;
        Stream<Integer> testStream = IntStream.rangeClosed(0, number).boxed();
        return testStream.flatMap(k -> {
            int[] profit = new int[nbVars];
            int[] weight = new int[nbVars];
            for (int i = 0; i < nbVars; i++) {
                profit[i] = 1 + rand.nextInt(cap / 2);
                weight[i] = 2 + rand.nextInt(cap / 2);
            }
            KSProblem pb = new KSProblem(cap, profit, weight, 0.0);
            return Stream.of(pb);
        });
    }

    private static double optimalSolutionNoCaching(KSProblem problem) {
        SolverConfig<Integer> config = new SolverConfig<>();
        config.problem = problem;
        config.relax = new KSRelax();
        config.flb = new KSFastLowerBound(problem);
        config.ranking = new KSRanking();
        config.width = new FixedWidth<>(10000);
        config.varh = new DefaultVariableHeuristic<>();
        config.frontier = new SimpleFrontier<>(config.ranking, CutSetType.LastExactLayer);

        final Solver solver = new SequentialSolver<>(config);

        solver.minimize(s -> false, (sol, s) -> {
        });
        return solver.bestValue().get();
    }


    private double optimalSolutionWithCache(KSProblem problem, int w, CutSetType cutSetType) {
        SolverConfig<Integer> config = new SolverConfig<>();
        config.problem = problem;
        config.relax = new KSRelax();
        config.flb = new KSFastLowerBound(problem);
        config.ranking = new KSRanking();
        config.width = new FixedWidth<>(w);
        config.varh = new DefaultVariableHeuristic<>();
        config.cache = new SimpleCache<>();
        config.frontier = new SimpleFrontier<>(config.ranking, cutSetType);


        final Solver solverWithCaching = new SequentialSolver<>(config);

        solverWithCaching.minimize(s -> false, (sol, s) -> {
        });
        return solverWithCaching.bestValue().get();
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testOptimalSolutionFound(KSProblem problem) {
        CutSetType[] cs = new CutSetType[]{CutSetType.LastExactLayer, CutSetType.Frontier};
        for (int wid = 1; wid <= 10; wid++) {
            for (CutSetType ct : cs) {
                assertEquals(optimalSolutionNoCaching(problem), optimalSolutionWithCache(problem, wid, ct));
            }
        }
    }
}
