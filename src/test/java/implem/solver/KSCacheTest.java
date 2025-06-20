package implem.solver;

import org.ddolib.ddo.core.CutSetType;
import org.ddolib.ddo.core.Frontier;
import org.ddolib.ddo.core.Solver;
import org.ddolib.ddo.examples.knapsack.KSProblem;
import org.ddolib.ddo.examples.knapsack.KSRanking;
import org.ddolib.ddo.examples.knapsack.KSRelax;
import org.ddolib.ddo.heuristics.VariableHeuristic;
import org.ddolib.ddo.implem.cache.SimpleCache;
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
        int number = 100;
        boolean found = false;
        int nbVars = 5; int cap = 10;
        Stream<Integer> testStream = IntStream.rangeClosed(0, number).boxed();
        return testStream.flatMap(k -> {
            int[] profit = new int[nbVars];
            int[] weight = new int[nbVars];
            for (int i = 0; i < nbVars; i++) {
                profit[i] = 1 + rand.nextInt(cap/2);
                weight[i] = 2 + rand.nextInt(cap/2);
            }
            KSProblem pb = new KSProblem(cap, profit, weight, null);
//            System.out.println(pb);
            return Stream.of(pb);
        });
    }

    @MethodSource("dataProvider")
    public int testOptimalSolution1(KSProblem problem) {
        final KSRelax relax = new KSRelax(problem);
        final KSRanking ranking = new KSRanking();
        final FixedWidth<Integer> width = new FixedWidth<>(3);
        final VariableHeuristic<Integer> varh = new DefaultVariableHeuristic<Integer>();
        final Frontier<Integer> frontier = new SimpleFrontier<>(ranking, CutSetType.LastExactLayer);
        final Solver solver1 = new SequentialSolver(
                problem,
                relax,
                varh,
                ranking,
                width,
                frontier);

        solver1.maximize();
        return solver1.bestValue().get();
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testOptimalSolution2(KSProblem problem) {
        final KSRelax relax = new KSRelax(problem);
        final KSRanking ranking = new KSRanking();
        final FixedWidth<Integer> width = new FixedWidth<>(3);
        final VariableHeuristic<Integer> varh = new DefaultVariableHeuristic<Integer>();
        final SimpleCache<Integer> cache = new SimpleCache();
        final Frontier<Integer> frontier = new SimpleFrontier<>(ranking, CutSetType.LastExactLayer);
        final Solver solver2 = new SequentialSolverCache(
                problem,
                relax,
                varh,
                ranking,
                width,
                cache,
                frontier);

        solver2.maximize();
        assertEquals(testOptimalSolution1(problem), solver2.bestValue().get());
        if (solver2.bestValue().get() != testOptimalSolution1(problem))
            System.out.println(problem);
    }
}
