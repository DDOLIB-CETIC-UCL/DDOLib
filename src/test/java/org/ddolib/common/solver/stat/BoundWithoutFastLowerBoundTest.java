package org.ddolib.common.solver.stat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Stream;
import org.ddolib.examples.layered.knapsack.KSProblem;
import org.ddolib.examples.layered.tsp.TSPProblem;
import org.ddolib.examples.layered.tsp.TSPState;
import org.ddolib.examples.nolayer.knapsack.KSState;
import org.ddolib.layered.modeling.AcsModel;
import org.ddolib.layered.modeling.AwAstarModel;
import org.ddolib.layered.modeling.ExactModel;
import org.ddolib.layered.modeling.Problem;
import org.ddolib.layered.modeling.Solvers;
import org.ddolib.layered.solver.Solution;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests of the bound and of the gap reported by A*, AWA* and ACS when the model does not provide
 * a fast lower bound.
 */
public class BoundWithoutFastLowerBoundTest {

    /** Runs a solver without fast lower bound, stopped after a few iterations. */
    private interface Run {
        <T> Solution apply(Problem<T> problem, int maxIterations);
    }

    private static <T> AwAstarModel<T> awAstarModel(Problem<T> problem) {
        return new AwAstarModel<>() {
            @Override
            public Problem<T> problem() {
                return problem;
            }
        };
    }

    private static <T> AcsModel<T> acsModel(Problem<T> problem) {
        return new AcsModel<>() {
            @Override
            public Problem<T> problem() {
                return problem;
            }
        };
    }

    static Stream<Arguments> solvers() {
        Run astar =
                new Run() {
                    @Override
                    public <T> Solution apply(Problem<T> p, int it) {
                        return Solvers.minimizeAstar(awAstarModel(p), s -> s.nbIterations() > it);
                    }
                };
        Run awastar =
                new Run() {
                    @Override
                    public <T> Solution apply(Problem<T> p, int it) {
                        return Solvers.minimizeAwAStar(awAstarModel(p), s -> s.nbIterations() > it);
                    }
                };
        Run acs =
                new Run() {
                    @Override
                    public <T> Solution apply(Problem<T> p, int it) {
                        return Solvers.minimizeAcs(acsModel(p), s -> s.nbIterations() > it);
                    }
                };
        return Stream.of(
                Arguments.of("A*", astar), Arguments.of("AWA*", awastar), Arguments.of("ACS", acs));
    }

    private static <T> ExactModel<T> exactModel(Problem<T> problem) {
        return new ExactModel<>() {
            @Override
            public Problem<T> problem() {
                return problem;
            }
        };
    }

    private static <T> double optimum(Problem<T> problem) {
        return Solvers.minimizeExact(exactModel(problem)).value();
    }

    /** The gap must never be smaller than the true distance from the incumbent to the optimum. */
    private static void assertValid(SearchStatistics stats, double optimum) {
        assertTrue(stats.bestBound() <= optimum + 1e-9, "the bound exceeds the optimum");
        if (!Double.isInfinite(stats.incumbent()) && stats.incumbent() != 0) {
            double incumbent = stats.incumbent();
            double trueGap = 100 * Math.abs(incumbent - optimum) / Math.abs(incumbent);
            assertTrue(stats.gap() + 1e-9 >= trueGap, "gap " + stats.gap() + " < " + trueGap);
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("solvers")
    void testNegativeCostsGiveNoFalseBound(String name, Run solver) throws IOException {
        // knapsack: the profits are negated, the costs are negative. Using the cost of the best
        // open node as bound gave a gap below the true gap (instance_test_2, A*, 28 iterations)
        for (String instance : new String[] {"instance_test_0", "instance_test_2"}) {
            KSProblem problem =
                    new KSProblem(
                            Path.of("src", "test", "resources", "Knapsack", instance).toString());
            double optimum = problem.optimalValue().orElseThrow();
            for (int it = 1; it <= 60; it++) {
                SearchStatistics stats = solver.apply(problem, it).statistics();
                assertValid(stats, optimum);
                if (stats.status() != SearchStatus.OPTIMAL) {
                    assertTrue(Double.isInfinite(stats.bestBound()), "no bound is known");
                }
            }
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("solvers")
    void testNonNegativeCostsGiveAValidBound(String name, Run solver) {
        Random random = new Random(5);
        int n = 8;
        double[][] distances = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                distances[i][j] = distances[j][i] = 1 + random.nextInt(20);
            }
        }
        TSPProblem problem =
                new TSPProblem(distances) {
                    @Override
                    public boolean hasNonNegativeCosts() {
                        return true;
                    }
                };
        double optimum = optimum(problem);
        SearchStatistics stats = solver.apply(problem, 5).statistics();

        assertFalse(Double.isInfinite(stats.bestBound()), "the bound should be known");
        assertValid(stats, optimum);
    }

    private static org.ddolib.nolayer.modeling.AcsModel<KSState> noLayerAcsModel(
            org.ddolib.examples.nolayer.knapsack.KSProblem problem) {
        return new org.ddolib.nolayer.modeling.AcsModel<>() {
            @Override
            public org.ddolib.nolayer.modeling.Problem<KSState> problem() {
                return problem;
            }
        };
    }

    @Test
    void testNoLayerAcsDetectsTheDefaultBound() throws IOException {
        // the no-layer default bound returns 0: with negative costs it must not be used as a bound
        double optimum =
                new KSProblem(
                                Path.of("src", "test", "resources", "Knapsack", "instance_test_0")
                                        .toString())
                        .optimalValue()
                        .orElseThrow();
        var problem =
                org.ddolib.examples.nolayer.knapsack.KSProblem.fromFile(
                        Path.of("src", "test", "resources", "Knapsack", "instance_test_0")
                                .toString());
        Function<Integer, SearchStatistics> run =
                it ->
                        org.ddolib.nolayer.modeling.Solvers.minimizeAcs(
                                        noLayerAcsModel(problem), s -> s.nbIterations() > it)
                                .statistics();
        for (int it : new int[] {2, 5, 10}) {
            SearchStatistics stats = run.apply(it);
            assertValid(stats, optimum);
            assertEquals(
                    stats.status() == SearchStatus.OPTIMAL, !Double.isInfinite(stats.bestBound()));
        }
    }
}
