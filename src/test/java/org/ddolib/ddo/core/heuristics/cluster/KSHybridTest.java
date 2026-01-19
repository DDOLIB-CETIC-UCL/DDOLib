package org.ddolib.ddo.core.heuristics.cluster;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.common.solver.Solution;
import org.ddolib.common.solver.Solver;
import org.ddolib.ddo.core.cache.SimpleCache;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.Frontier;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.ddo.core.solver.SequentialSolver;
import org.ddolib.examples.knapsack.*;
import org.ddolib.modeling.*;
import org.ddolib.util.verbosity.VerbosityLevel;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
/**
 * Unit tests for evaluating the Hybrid reduction strategy on Knapsack problems (KSProblem)
 * using Decision Diagram Optimization (DDO).
 *
 * <p>
 * This test class compares the optimal solutions obtained using two different
 * reduction strategies:
 * <ul>
 *   <li>Cost-based clustering</li>
 *   <li>Hybrid clustering (combining cost-based and distance-based GHP methods)</li>
 * </ul>
 *
 * <p>
 * The tests ensure that the Hybrid strategy produces solutions equivalent
 * to the cost-based method across a range of knapsack instances, layer widths,
 * and cut set types.
 *
 * <p>
 * The class uses JUnit 5 {@link org.junit.jupiter.params.ParameterizedTest} and
 * {@link org.junit.jupiter.params.provider.MethodSource} for generating test
 * instances.
 */
public class KSHybridTest {
    /**
     * Generates a stream of KSProblem instances for testing.
     *
     * <p>
     * Each instance has random profits and weights for a fixed number of variables,
     * and a fixed knapsack capacity.
     *
     * @return a stream of {@link KSProblem} instances
     * @throws IOException if any I/O error occurs (not expected here)
     */
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
    /**
     * Computes the optimal solution using cost-based clustering strategy.
     *
     * @param problem the knapsack problem instance
     * @return the optimal objective value
     */
    private static double optimalSolutionCostBasedClustering(KSProblem problem) {
        final DdoModel<Integer> model = new DdoModel<>() {
            ;

            @Override
            public Problem<Integer> problem() {
                return problem;
            }

            @Override
            public Relaxation<Integer> relaxation() {
                return new KSRelax();
            }

            @Override
            public KSRanking ranking() {
                return new KSRanking();
            }

            @Override
            public FastLowerBound<Integer> lowerBound() {
                return new KSFastLowerBound(problem);
            }

            @Override
            public DominanceChecker<Integer> dominance() {
                return new SimpleDominanceChecker<>(new KSDominance(), problem.nbVars());
            }

            @Override
            public boolean useCache() {
                return false;
            }

            @Override
            public WidthHeuristic<Integer> widthHeuristic() {
                return new FixedWidth<>(10_000);
            }

            @Override
            public VerbosityLevel verbosityLevel() {
                return VerbosityLevel.SILENT;
            }

            @Override
            public ReductionStrategy<Integer> relaxStrategy() {
                return new CostBased<>(ranking());
            }

            @Override
            public ReductionStrategy<Integer> restrictStrategy() {
                return new CostBased<>(ranking());
            }
        };

        Solution solution = Solvers.minimizeDdo(model);

        return solution.value();
    }

    /**
     * Computes the optimal solution using the Hybrid clustering strategy.
     *
     * <p>
     * The Hybrid strategy combines cost-based ranking and distance-based GHP
     * clustering to reduce the width of the decision diagram.
     *
     * @param problem the knapsack problem instance
     * @param w the maximum width of the decision diagram
     * @param cutSetType the type of cut set used in frontier-based DDO
     * @return the optimal objective value
     */
    private double optimalSolutionHybridClustering(KSProblem problem, int w, CutSetType cutSetType) {
        final DdoModel<Integer> model = new DdoModel<>() {
            ;

            @Override
            public Problem<Integer> problem() {
                return problem;
            }

            @Override
            public Relaxation<Integer> relaxation() {
                return new KSRelax();
            }

            @Override
            public KSRanking ranking() {
                return new KSRanking();
            }

            @Override
            public FastLowerBound<Integer> lowerBound() {
                return new KSFastLowerBound(problem);
            }

            @Override
            public DominanceChecker<Integer> dominance() {
                return new SimpleDominanceChecker<>(new KSDominance(), problem.nbVars());
            }

            @Override
            public boolean useCache() {
                return false;
            }

            @Override
            public WidthHeuristic<Integer> widthHeuristic() {
                return new FixedWidth<>(w);
            }

            @Override
            public Frontier<Integer> frontier() {
                return new SimpleFrontier<>(ranking(), cutSetType);
            }

            @Override
            public VerbosityLevel verbosityLevel() {
                return VerbosityLevel.SILENT;
            }

            @Override
            public ReductionStrategy<Integer> relaxStrategy() {
                return new Hybrid<>(new KSRanking(), new KSDistance(problem));
            }

            @Override
            public ReductionStrategy<Integer> restrictStrategy() {
                return new Hybrid<>(new KSRanking(), new KSDistance(problem));
            }
        };

        Solution solution = Solvers.minimizeDdo(model);

        return solution.value();
    }
    /**
     * Parameterized test that compares the solutions obtained with Hybrid clustering
     * against cost-based clustering for each test problem instance.
     *
     * <p>
     * The test iterates over different layer widths and cut set types, asserting
     * that the Hybrid strategy produces the same optimal value as cost-based clustering.
     *
     * @param problem a knapsack problem instance from the data provider
     */
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testOptimalSolutionFound(KSProblem problem) {
        CutSetType[] cs = new CutSetType[]{CutSetType.LastExactLayer, CutSetType.Frontier};
        for (int wid = 2; wid <= 10; wid++) {
            for (CutSetType ct : cs) {
                assertEquals(optimalSolutionCostBasedClustering(problem), optimalSolutionHybridClustering(problem, wid, ct));
            }
        }
    }

}
