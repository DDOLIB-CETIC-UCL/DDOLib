package org.ddolib.ddo.core.cache;

import org.ddolib.common.dominance.DefaultDominanceChecker;
import org.ddolib.common.solver.Solver;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.Frontier;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.ddo.core.heuristics.variable.VariableHeuristic;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.solver.SequentialSolver;
import org.ddolib.ddo.core.solver.SequentialSolverWithCache;
import org.ddolib.examples.ddo.knapsack.KSFastUpperBound;
import org.ddolib.examples.ddo.knapsack.KSProblem;
import org.ddolib.examples.ddo.knapsack.KSRanking;
import org.ddolib.examples.ddo.knapsack.KSRelax;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;

//import static org.ddolib.ddo.implem.solver.Solvers.sequentialSolver;
//import static org.ddolib.ddo.implem.solver.Solvers.sequentialSolverWithCache;
import static org.ddolib.factory.Solvers.sequentialSolver;
import static org.ddolib.factory.Solvers.sequentialSolverWithCache;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class KSCacheTest {

    static Stream<KSProblem> dataProvider() throws IOException {
        Random rand = new Random(10);
        int number = 1000;
        boolean found = false;
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
//            System.out.println(pb);
            return Stream.of(pb);
        });
    }

    private static double optimalSolutionNoCaching(KSProblem problem) {
        final KSRelax relax = new KSRelax();
        final KSRanking ranking = new KSRanking();
        final KSFastUpperBound fub = new KSFastUpperBound(problem);
        final FixedWidth<Integer> width = new FixedWidth<>(10000);
        final VariableHeuristic<Integer> varh = new DefaultVariableHeuristic<Integer>();
        final Frontier<Integer> frontier = new SimpleFrontier<>(ranking, CutSetType.LastExactLayer);
        final DefaultDominanceChecker<Integer> dominance = new DefaultDominanceChecker<Integer>();
        final Solver solver1 = sequentialSolver(
                problem,
                relax,
                varh,
                ranking,
                width,
                frontier,
                fub,
                dominance);

        solver1.maximize();
        return solver1.bestValue().get();
    }


    private double optimalSolutionWithCache(KSProblem problem, int w, CutSetType cutSetType) {
        final KSRelax relax = new KSRelax();
        final KSRanking ranking = new KSRanking();
        final FixedWidth<Integer> width = new FixedWidth<>(w);
        final VariableHeuristic<Integer> varh = new DefaultVariableHeuristic<Integer>();
        final KSFastUpperBound fub = new KSFastUpperBound(problem);

        final SimpleCache<Integer> cache = new SimpleCache<>();
        final Frontier<Integer> frontier = new SimpleFrontier<>(ranking, cutSetType);
        final DefaultDominanceChecker<Integer> dominance = new DefaultDominanceChecker<>();
        final Solver solverWithCaching = sequentialSolverWithCache(
                problem,
                relax,
                varh,
                ranking,
                width,
                frontier,
                fub,
                dominance,
                cache);

        solverWithCaching.maximize();
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
