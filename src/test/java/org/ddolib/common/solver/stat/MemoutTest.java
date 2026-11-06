package org.ddolib.common.solver.stat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.ddolib.common.heuristics.width.FixedWidth;
import org.ddolib.common.heuristics.width.WidthHeuristic;
import org.ddolib.common.solver.stopcriterion.StopCriterion;
import org.ddolib.common.util.InvalidSolutionException;
import org.ddolib.examples.layered.knapsack.KSDominance;
import org.ddolib.examples.layered.knapsack.KSFastLowerBound;
import org.ddolib.examples.layered.knapsack.KSProblem;
import org.ddolib.examples.layered.knapsack.KSRanking;
import org.ddolib.examples.layered.knapsack.KSRelax;
import org.ddolib.layered.modeling.AcsModel;
import org.ddolib.layered.modeling.AwAstarModel;
import org.ddolib.layered.modeling.DdoModel;
import org.ddolib.layered.modeling.DominanceChecker;
import org.ddolib.layered.modeling.ExactModel;
import org.ddolib.layered.modeling.FastLowerBound;
import org.ddolib.layered.modeling.LnsModel;
import org.ddolib.layered.modeling.Problem;
import org.ddolib.layered.modeling.Relaxation;
import org.ddolib.layered.modeling.SimpleDominanceChecker;
import org.ddolib.layered.modeling.Solvers;
import org.ddolib.layered.modeling.StateRanking;
import org.ddolib.layered.solver.Solution;
import org.ddolib.layered.solving.ddo.core.Decision;
import org.ddolib.layered.solving.startheuristic.DFS;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests of the status {@link SearchStatus#MEMOUT}: when the memory runs out, the solvers return
 * the best solution found so far instead of throwing an {@link OutOfMemoryError}.
 */
public class MemoutTest {

    private static final String INSTANCE =
            Path.of("src", "test", "resources", "Knapsack", "instance_test_0").toString();

    /**
     * A knapsack simulating an out of memory: its transitions throw an {@link OutOfMemoryError}
     * once a given number of transitions has been applied.
     */
    private static final class FailingProblem implements Problem<Integer> {
        private final KSProblem problem;
        private final long budget;
        private long nbTransitions = 0;

        FailingProblem(KSProblem problem, long budget) {
            this.problem = problem;
            this.budget = budget;
        }

        @Override
        public int nbVars() {
            return problem.nbVars();
        }

        @Override
        public Integer initialState() {
            return problem.initialState();
        }

        @Override
        public double initialValue() {
            return problem.initialValue();
        }

        @Override
        public Iterator<Integer> domain(Integer state, int var) {
            return problem.domain(state, var);
        }

        @Override
        public Integer transition(Integer state, Decision decision) {
            if (++nbTransitions > budget) {
                throw new OutOfMemoryError("simulated out of memory");
            }
            return problem.transition(state, decision);
        }

        @Override
        public double transitionCost(Integer state, Decision decision) {
            return problem.transitionCost(state, decision);
        }

        @Override
        public Optional<Double> optimalValue() {
            return problem.optimalValue();
        }

        @Override
        public double evaluate(int[] solution) throws InvalidSolutionException {
            return problem.evaluate(solution);
        }
    }

    /** A solver run on a problem with a limit and a callback for the solutions. */
    @FunctionalInterface
    private interface Run {
        Solution apply(
                FailingProblem problem,
                Predicate<SearchStatistics> limit,
                BiConsumer<int[], SearchStatistics> onSolution);
    }

    private static DdoModel<Integer> ddoModel(FailingProblem problem) {
        return new DdoModel<>() {
            @Override
            public Problem<Integer> problem() {
                return problem;
            }

            @Override
            public FastLowerBound<Integer> lowerBound() {
                return new KSFastLowerBound(problem.problem);
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

    private static AwAstarModel<Integer> awAstarModel(FailingProblem problem) {
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

    private static AcsModel<Integer> acsModel(FailingProblem problem) {
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

    private static ExactModel<Integer> exactModel(FailingProblem problem) {
        return new ExactModel<>() {
            @Override
            public Problem<Integer> problem() {
                return problem;
            }
        };
    }

    private static LnsModel<Integer> lnsModel(FailingProblem problem) {
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
                return base.widthHeuristic();
            }
        };
    }

    private static final Run ASTAR = (p, l, s) -> Solvers.minimizeAstar(awAstarModel(p), l, s);
    private static final Run AWASTAR = (p, l, s) -> Solvers.minimizeAwAStar(awAstarModel(p), l, s);
    private static final Run ACS = (p, l, s) -> Solvers.minimizeAcs(acsModel(p), l, s);
    private static final Run DDO = (p, l, s) -> Solvers.minimizeDdo(ddoModel(p), l, s);
    private static final Run LNS =
            (p, l, s) -> Solvers.minimizeLns(lnsModel(p), l.or(st -> st.nbIterations() > 50), s);
    private static final Run DFS = (p, l, s) -> new DFS<>(awAstarModel(p)).findSolution(l);
    private static final Run EXACT = (p, l, s) -> Solvers.minimizeExact(exactModel(p), s);
    private static final Run RELAXED = (p, l, s) -> Solvers.relaxedDdo(ddoModel(p), l, s);
    private static final Run RESTRICTED = (p, l, s) -> Solvers.restrictedDdo(ddoModel(p), l, s);

    static Stream<Arguments> searches() {
        return Stream.of(
                Arguments.of("A*", ASTAR),
                Arguments.of("AWA*", AWASTAR),
                Arguments.of("ACS", ACS),
                Arguments.of("DDO", DDO),
                Arguments.of("LNS", LNS),
                Arguments.of("DFS", DFS));
    }

    static Stream<Arguments> allSolvers() {
        return Stream.concat(
                searches(),
                Stream.of(
                        Arguments.of("Exact", EXACT),
                        Arguments.of("Relaxed", RELAXED),
                        Arguments.of("Restricted", RESTRICTED)));
    }

    private static KSProblem knapsack() throws IOException {
        return new KSProblem(INSTANCE);
    }

    /** Records the last solution given to the callback. */
    private static final class LastSolution implements BiConsumer<int[], SearchStatistics> {
        private int[] solution = null;
        private SearchStatistics statistics = null;

        @Override
        public void accept(int[] sol, SearchStatistics stats) {
            solution = sol.clone();
            statistics = stats;
        }
    }

    /**
     * Checks that the result is a memory out returning the best solution given to the callback,
     * with a valid lower bound.
     */
    private static void assertMemout(Solution sol, LastSolution last, double optimum)
            throws IOException, InvalidSolutionException {
        SearchStatistics stats = sol.statistics();
        assertEquals(SearchStatus.MEMOUT, stats.status());
        assertTrue(stats.bestBound() <= optimum + 1e-9);
        if (last.solution != null) {
            assertTrue(!sol.isEmpty());
            assertEquals(last.statistics.incumbent(), stats.incumbent(), 1e-9);
            assertEquals(stats.incumbent(), knapsack().evaluate(sol.solution()), 1e-9);
        }
        if (!sol.isEmpty()) {
            assertEquals(stats.incumbent(), knapsack().evaluate(sol.solution()), 1e-9);
        }
    }

    private static double optimum() throws IOException {
        return Solvers.minimizeExact(exactModel(new FailingProblem(knapsack(), Long.MAX_VALUE)))
                .value();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("searches")
    void testFrontierLimitReturnsMemout(String name, Run run) throws Exception {
        FailingProblem problem = new FailingProblem(knapsack(), Long.MAX_VALUE);
        LastSolution last = new LastSolution();
        Solution sol = run.apply(problem, StopCriterion.maxFrontierSize(3), last);

        assertMemout(sol, last, optimum());
        assertTrue(sol.statistics().frontierMaxSize() >= 3);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("searches")
    void testFrontierLimitCombinedWithAnotherCriterion(String name, Run run) throws Exception {
        FailingProblem problem = new FailingProblem(knapsack(), Long.MAX_VALUE);
        LastSolution last = new LastSolution();
        Predicate<SearchStatistics> limit =
                StopCriterion.maxTime(60_000).or(StopCriterion.maxFrontierSize(3));
        Solution sol = run.apply(problem, limit, last);

        assertMemout(sol, last, optimum());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("allSolvers")
    void testOutOfMemoryReturnsMemout(String name, Run run) throws Exception {
        // number of transitions of a complete run
        FailingProblem unlimited = new FailingProblem(knapsack(), Long.MAX_VALUE);
        run.apply(unlimited, s -> false, (x, s) -> {});
        long budget = unlimited.nbTransitions / 2;

        FailingProblem problem = new FailingProblem(knapsack(), budget);
        LastSolution last = new LastSolution();
        Solution sol = run.apply(problem, s -> false, last);

        assertMemout(sol, last, optimum());
        assertTrue(problem.nbTransitions > budget, "the out of memory was not simulated");
    }

    static Stream<Arguments> anytimeSolvers() {
        return Stream.of(
                Arguments.of("AWA*", AWASTAR), Arguments.of("DDO", DDO), Arguments.of("LNS", LNS));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("anytimeSolvers")
    void testBestSolutionIsReturnedOnOutOfMemory(String name, Run run) throws Exception {
        FailingProblem unlimited = new FailingProblem(knapsack(), Long.MAX_VALUE);
        run.apply(unlimited, s -> false, (x, s) -> {});
        // the out of memory happens at the last transition, after a first solution is found
        FailingProblem problem = new FailingProblem(knapsack(), unlimited.nbTransitions - 1);
        LastSolution last = new LastSolution();
        Solution sol = run.apply(problem, s -> false, last);

        assertTrue(last.solution != null, "no solution found before the out of memory");
        assertMemout(sol, last, optimum());
        assertTrue(sol.statistics().incumbent() >= optimum() - 1e-9);
    }
}
