package implem.solver;

import org.ddolib.ddo.core.CutSetType;
import org.ddolib.ddo.core.Frontier;
import org.ddolib.ddo.core.SearchStatistics;
import org.ddolib.ddo.core.Solver;
import org.ddolib.ddo.examples.boundedknapsack.BKSDominance;
import org.ddolib.ddo.examples.knapsack.KSProblem;
import org.ddolib.ddo.examples.knapsack.KSRanking;
import org.ddolib.ddo.examples.knapsack.KSRelax;
import org.ddolib.ddo.heuristics.VariableHeuristic;
import org.ddolib.ddo.implem.cache.SimpleCache;
import org.ddolib.ddo.implem.dominance.SimpleDominanceChecker;
import org.ddolib.ddo.implem.frontier.SimpleFrontier;
import org.ddolib.ddo.implem.heuristics.DefaultVariableHeuristic;
import org.ddolib.ddo.implem.heuristics.FixedWidth;
import org.ddolib.ddo.implem.solver.SequentialSolver;
import org.ddolib.ddo.implem.solver.SequentialSolverCache;
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
        boolean found = false;
        int nbVars = 14; int cap = 10;
        Stream<Integer> testStream = IntStream.rangeClosed(0, number).boxed();
        return testStream.flatMap(k -> {
            int[] profit = new int[nbVars];
            int[] weight = new int[nbVars];
            for (int i = 0; i < nbVars; i++) {
                profit[i] = 1 + rand.nextInt(cap/2);
                weight[i] = 2 + rand.nextInt(cap/2);
            }
            KSProblem pb = new KSProblem(cap, profit, weight, Double.MAX_VALUE);
//            System.out.println(pb);
            return Stream.of(pb);
        });
    }

    private static int optimalSolutionNoCaching(KSProblem problem) {
        final KSRelax relax = new KSRelax(problem);
        final KSRanking ranking = new KSRanking();
        final FixedWidth<Integer> width = new FixedWidth<>(4);
        final VariableHeuristic<Integer> varh = new DefaultVariableHeuristic<Integer>();
        final SimpleDominanceChecker<Integer, Integer> dominance = new SimpleDominanceChecker<>(new BKSDominance(), problem.nbVars());
        final Frontier<Integer> frontier = new SimpleFrontier<>(ranking, CutSetType.Frontier);
        final Solver solver1 = new SequentialSolver(
                problem,
                relax,
                varh,
                ranking,
                width,
                frontier,
                dominance);

        solver1.maximize();
        return solver1.bestValue().get().intValue();
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testOptimalSolutionFound(KSProblem problem) {
        final KSRelax relax = new KSRelax(problem);
        final KSRanking ranking = new KSRanking();
        final FixedWidth<Integer> width = new FixedWidth<>(3);
        final VariableHeuristic<Integer> varh = new DefaultVariableHeuristic<Integer>();
        final SimpleCache<Integer> cache = new SimpleCache();
        final Frontier<Integer> frontier = new SimpleFrontier<>(ranking, CutSetType.Frontier);
        final Solver solverWithCaching = new SequentialSolverCache(
                problem,
                relax,
                varh,
                ranking,
                width,
                cache,
                frontier);

        SearchStatistics stats = solverWithCaching.maximize();
//        System.out.println("optimal Solution No Caching : " + optimalSolutionNoCaching(problem) + "\nsolver With Caching: " + solverWithCaching.bestValue().get());

        assertEquals(optimalSolutionNoCaching(problem), solverWithCaching.bestValue().get());
        if (optimalSolutionNoCaching(problem) != solverWithCaching.bestValue().get()) {
            System.out.println(problem);
        }
    }

}
