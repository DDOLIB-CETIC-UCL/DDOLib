package org.ddolib.common.solver.stat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.ddolib.common.heuristics.width.FixedWidth;
import org.ddolib.common.heuristics.width.WidthHeuristic;
import org.ddolib.examples.layered.knapsack.KSDominance;
import org.ddolib.examples.layered.knapsack.KSFastLowerBound;
import org.ddolib.examples.layered.knapsack.KSProblem;
import org.ddolib.examples.layered.knapsack.KSRanking;
import org.ddolib.examples.layered.knapsack.KSRelax;
import org.ddolib.examples.nolayer.knapsack.KSModel;
import org.ddolib.layered.modeling.AcsModel;
import org.ddolib.layered.modeling.AwAstarModel;
import org.ddolib.layered.modeling.DdoModel;
import org.ddolib.layered.modeling.DominanceChecker;
import org.ddolib.layered.modeling.FastLowerBound;
import org.ddolib.layered.modeling.LnsModel;
import org.ddolib.layered.modeling.Problem;
import org.ddolib.layered.modeling.Relaxation;
import org.ddolib.layered.modeling.SimpleDominanceChecker;
import org.ddolib.layered.modeling.Solvers;
import org.ddolib.layered.modeling.StateRanking;
import org.ddolib.layered.solver.Solution;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/** Tests of {@link SearchStatistics#bestBound()} for every solver. */
public class BestBoundTest {

    private static final String INSTANCE =
            Path.of("src", "test", "resources", "Knapsack", "instance_test_0").toString();

    private static DdoModel<Integer> ddoModel(KSProblem problem) {
        return new DdoModel<>() {
            @Override
            public Problem<Integer> problem() {
                return problem;
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
            public Relaxation<Integer> relaxation() {
                return new KSRelax();
            }

            @Override
            public StateRanking<Integer> ranking() {
                return new KSRanking();
            }

            @Override
            public WidthHeuristic<Integer> widthHeuristic() {
                return new FixedWidth<>(2);
            }
        };
    }

    private static AwAstarModel<Integer> awAstarModel(KSProblem problem) {
        DdoModel<Integer> base = ddoModel(problem);
        return new AwAstarModel<>() {
            @Override
            public Problem<Integer> problem() {
                return problem;
            }

            @Override
            public FastLowerBound<Integer> lowerBound() {
                return base.lowerBound();
            }

            @Override
            public DominanceChecker<Integer> dominance() {
                return base.dominance();
            }
        };
    }

    private static AcsModel<Integer> acsModel(KSProblem problem) {
        DdoModel<Integer> base = ddoModel(problem);
        return new AcsModel<>() {
            @Override
            public Problem<Integer> problem() {
                return problem;
            }

            @Override
            public FastLowerBound<Integer> lowerBound() {
                return base.lowerBound();
            }

            @Override
            public DominanceChecker<Integer> dominance() {
                return base.dominance();
            }
        };
    }

    private static LnsModel<Integer> lnsModel(KSProblem problem, int width) {
        DdoModel<Integer> base = ddoModel(problem);
        return new LnsModel<>() {
            @Override
            public Problem<Integer> problem() {
                return problem;
            }

            @Override
            public FastLowerBound<Integer> lowerBound() {
                return base.lowerBound();
            }

            @Override
            public DominanceChecker<Integer> dominance() {
                return base.dominance();
            }

            @Override
            public StateRanking<Integer> ranking() {
                return base.ranking();
            }

            @Override
            public WidthHeuristic<Integer> widthHeuristic() {
                return new FixedWidth<>(width);
            }
        };
    }

    static Stream<Arguments> solvers() {
        Function<Predicate<SearchStatistics>, Function<KSProblem, Solution>> astar =
                limit -> p -> Solvers.minimizeAstar(awAstarModel(p), limit);
        Function<Predicate<SearchStatistics>, Function<KSProblem, Solution>> awastar =
                limit -> p -> Solvers.minimizeAwAStar(awAstarModel(p), limit);
        Function<Predicate<SearchStatistics>, Function<KSProblem, Solution>> acs =
                limit -> p -> Solvers.minimizeAcs(acsModel(p), limit);
        Function<Predicate<SearchStatistics>, Function<KSProblem, Solution>> ddo =
                limit -> p -> Solvers.minimizeDdo(ddoModel(p), limit);
        // the LNS only stops by itself when the DD compiled from the root is exact
        Function<Predicate<SearchStatistics>, Function<KSProblem, Solution>> lns =
                limit -> p -> Solvers.minimizeLns(lnsModel(p, 2), limit);
        Function<Predicate<SearchStatistics>, Function<KSProblem, Solution>> lnsExact =
                limit -> p -> Solvers.minimizeLns(lnsModel(p, 1_000_000), limit);
        return Stream.of(
                Arguments.of("A*", astar, astar),
                Arguments.of("AWA*", awastar, awastar),
                Arguments.of("ACS", acs, acs),
                Arguments.of("DDO", ddo, ddo),
                Arguments.of("LNS", lns, lnsExact));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("solvers")
    void testBoundIsKnownWhenTheSearchIsStoppedEarly(
            String name,
            Function<Predicate<SearchStatistics>, Function<KSProblem, Solution>> solver,
            Function<Predicate<SearchStatistics>, Function<KSProblem, Solution>> complete)
            throws IOException {
        KSProblem problem = new KSProblem(INSTANCE);
        double optimum = problem.optimalValue().orElseThrow();
        // stopped after 2 iterations, as a time limit would do
        SearchStatistics stats = solver.apply(s -> s.nbIterations() > 2).apply(problem).statistics();

        assertNotEquals(SearchStatus.OPTIMAL, stats.status());
        assertFalse(Double.isInfinite(stats.bestBound()), "the bound should be known");
        assertTrue(stats.bestBound() <= optimum + 1e-9, "the bound exceeds the optimum");
        assertTrue(stats.bestBound() <= stats.incumbent() + 1e-9);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("solvers")
    void testBoundIsTheOptimumWhenTheSearchIsComplete(
            String name,
            Function<Predicate<SearchStatistics>, Function<KSProblem, Solution>> early,
            Function<Predicate<SearchStatistics>, Function<KSProblem, Solution>> solver)
            throws IOException {
        KSProblem problem = new KSProblem(INSTANCE);
        double optimum = problem.optimalValue().orElseThrow();
        SearchStatistics stats = solver.apply(s -> false).apply(problem).statistics();

        assertEquals(SearchStatus.OPTIMAL, stats.status());
        assertEquals(optimum, stats.bestBound(), 1e-9);
    }

    @Test
    void testNoLayerAstarBoundIsKnownWhenTheSearchIsStoppedEarly() throws IOException {
        double optimum = new KSProblem(INSTANCE).optimalValue().orElseThrow();
        var problem = org.ddolib.examples.nolayer.knapsack.KSProblem.fromFile(INSTANCE);
        SearchStatistics stats =
                org.ddolib.nolayer.modeling.Solvers.minimizeAstar(
                                new KSModel(problem), s -> s.nbIterations() > 2)
                        .statistics();

        assertNotEquals(SearchStatus.OPTIMAL, stats.status());
        assertFalse(Double.isInfinite(stats.bestBound()));
        assertTrue(stats.bestBound() <= optimum + 1e-9);
    }
}
