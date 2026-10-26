package org.ddolib.layered.solver;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import org.ddolib.common.frontier.CutSetType;
import org.ddolib.common.solver.stat.DdoStats;
import org.ddolib.common.solver.stat.SearchStatistics;
import org.ddolib.common.solver.stat.SearchStatus;
import org.ddolib.common.util.debug.DebugLevel;
import org.ddolib.examples.layered.knapsack.KSDominance;
import org.ddolib.examples.layered.knapsack.KSFastLowerBound;
import org.ddolib.examples.layered.knapsack.KSProblem;
import org.ddolib.examples.layered.knapsack.KSRanking;
import org.ddolib.examples.layered.knapsack.KSRelax;
import org.ddolib.layered.modeling.AcsModel;
import org.ddolib.layered.modeling.AwAstarModel;
import org.ddolib.layered.modeling.DdoModel;
import org.ddolib.layered.modeling.DefaultFastLowerBound;
import org.ddolib.layered.modeling.DominanceChecker;
import org.ddolib.layered.modeling.FastLowerBound;
import org.ddolib.layered.modeling.Model;
import org.ddolib.layered.modeling.Problem;
import org.ddolib.layered.modeling.Relaxation;
import org.ddolib.layered.modeling.SimpleDominanceChecker;
import org.ddolib.layered.modeling.Solvers;
import org.ddolib.layered.solving.ddo.core.Decision;
import org.ddolib.layered.solving.ddo.core.heuristics.cluster.CostBased;
import org.ddolib.layered.solving.ddo.core.heuristics.cluster.ReductionStrategy;
import org.ddolib.layered.solving.ddo.core.heuristics.cluster.StateDistance;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/** Tests of the initial solution given to the solvers through {@link Model#initialSolution()}. */
class InitialSolutionTest {

    private static final KSProblem PROBLEM =
            new KSProblem(
                    20,
                    new int[] {10, 7, 8, 4, 6, 3, 9, 5, 2, 6},
                    new int[] {5, 4, 6, 3, 5, 2, 7, 4, 1, 4});

    /** Solvers able to start from an initial solution. */
    private enum Algo {
        ASTAR {
            @Override
            Solution solve(
                    Solution init,
                    Predicate<SearchStatistics> limit,
                    BiConsumer<int[], SearchStatistics> onSolution) {
                Model<Integer> model =
                        new Model<>() {
                            @Override
                            public Problem<Integer> problem() {
                                return PROBLEM;
                            }

                            @Override
                            public FastLowerBound<Integer> lowerBound() {
                                return new KSFastLowerBound(PROBLEM);
                            }

                            @Override
                            public DominanceChecker<Integer> dominance() {
                                return ksDominance();
                            }

                            @Override
                            public DebugLevel debugMode() {
                                return DebugLevel.ON;
                            }
                        };
                return Solvers.minimizeAstar(model.setInitialSolution(init), limit, onSolution);
            }
        },
        AWASTAR {
            @Override
            Solution solve(
                    Solution init,
                    Predicate<SearchStatistics> limit,
                    BiConsumer<int[], SearchStatistics> onSolution) {
                AwAstarModel<Integer> model =
                        new AwAstarModel<>() {
                            @Override
                            public Problem<Integer> problem() {
                                return PROBLEM;
                            }

                            @Override
                            public FastLowerBound<Integer> lowerBound() {
                                return new KSFastLowerBound(PROBLEM);
                            }

                            @Override
                            public DominanceChecker<Integer> dominance() {
                                return ksDominance();
                            }

                            @Override
                            public DebugLevel debugMode() {
                                return DebugLevel.ON;
                            }
                        };
                return Solvers.minimizeAwAStar(model.setInitialSolution(init), limit, onSolution);
            }
        },
        ACS {
            @Override
            Solution solve(
                    Solution init,
                    Predicate<SearchStatistics> limit,
                    BiConsumer<int[], SearchStatistics> onSolution) {
                AcsModel<Integer> model =
                        new AcsModel<>() {
                            @Override
                            public Problem<Integer> problem() {
                                return PROBLEM;
                            }

                            @Override
                            public FastLowerBound<Integer> lowerBound() {
                                return new KSFastLowerBound(PROBLEM);
                            }

                            @Override
                            public DominanceChecker<Integer> dominance() {
                                return ksDominance();
                            }

                            @Override
                            public DebugLevel debugMode() {
                                return DebugLevel.ON;
                            }
                        };
                return Solvers.minimizeAcs(model.setInitialSolution(init), limit, onSolution);
            }
        },
        DDO {
            @Override
            Solution solve(
                    Solution init,
                    Predicate<SearchStatistics> limit,
                    BiConsumer<int[], SearchStatistics> onSolution) {
                return Solvers.minimizeDdo(
                        ddoModel().setInitialSolution(init), limit, onSolution);
            }
        };

        abstract Solution solve(
                Solution init,
                Predicate<SearchStatistics> limit,
                BiConsumer<int[], SearchStatistics> onSolution);

        Solution solve(Solution init) {
            return solve(init, s -> false, (sol, s) -> {});
        }
    }

    private static DominanceChecker<Integer> ksDominance() {
        return new SimpleDominanceChecker<>(new KSDominance(), PROBLEM.nbVars());
    }

    private static DdoModel<Integer> ddoModel() {
        return new DdoModel<>() {
            @Override
            public Problem<Integer> problem() {
                return PROBLEM;
            }

            @Override
            public FastLowerBound<Integer> lowerBound() {
                return new KSFastLowerBound(PROBLEM);
            }

            @Override
            public DominanceChecker<Integer> dominance() {
                return ksDominance();
            }

            @Override
            public DebugLevel debugMode() {
                return DebugLevel.ON;
            }

            @Override
            public Relaxation<Integer> relaxation() {
                return new KSRelax();
            }

            @Override
            public KSRanking ranking() {
                return new KSRanking();
            }
        };
    }

    private static Solution optimalSolution() {
        return Solvers.minimizeExact(ddoModel().toExactModel());
    }

    /** Wraps an assignment of all the variables and its value in a {@link Solution}. */
    private static Solution toSolution(int[] assignment, double value) {
        Set<Decision> decisions = new HashSet<>();
        for (int i = 0; i < assignment.length; i++) {
            decisions.add(new Decision(i, assignment[i]));
        }
        return new Solution(Optional.of(decisions), new DdoStats(0, value));
    }

    /** Solution taking no item, of value 0. */
    private static Solution emptyKnapsack() {
        return toSolution(new int[PROBLEM.nbVars()], 0);
    }

    @ParameterizedTest
    @EnumSource(Algo.class)
    void testOptimalInitialSolutionIsReturned(Algo algo) {
        Solution optimal = optimalSolution();
        AtomicInteger nbCallbacks = new AtomicInteger();

        Solution sol = algo.solve(optimal, s -> false, (x, s) -> nbCallbacks.incrementAndGet());

        assertFalse(sol.isEmpty());
        assertEquals(optimal.value(), sol.value(), 1e-10);
        assertArrayEquals(optimal.solution(), sol.solution());
        assertEquals(SearchStatus.OPTIMAL, sol.statistics().status());
        assertEquals(0, nbCallbacks.get());
    }

    @ParameterizedTest
    @EnumSource(Algo.class)
    void testSuboptimalInitialSolutionIsImproved(Algo algo) {
        Solution sol = algo.solve(emptyKnapsack());

        assertEquals(optimalSolution().value(), sol.value(), 1e-10);
        assertEquals(SearchStatus.OPTIMAL, sol.statistics().status());
    }

    @ParameterizedTest
    @EnumSource(Algo.class)
    void testInitialSolutionKeptWhenInterrupted(Algo algo) {
        Solution init = emptyKnapsack();

        Solution sol = algo.solve(init, s -> true, (x, s) -> {});

        assertArrayEquals(init.solution(), sol.solution());
        assertEquals(init.value(), sol.value(), 1e-10);
        assertEquals(SearchStatus.SAT, sol.statistics().status());
    }

    @ParameterizedTest
    @EnumSource(Algo.class)
    void testEmptyInitialSolutionIsIgnored(Algo algo) {
        Solution noSolution = new Solution(Optional.empty(), new DdoStats(0, 0));

        Solution sol = algo.solve(noSolution);

        assertEquals(optimalSolution().value(), sol.value(), 1e-10);
    }

    @ParameterizedTest
    @EnumSource(Algo.class)
    void testInvalidValueRejectedInDebugMode(Algo algo) {
        Solution optimal = optimalSolution();
        Solution wrongValue = toSolution(optimal.solution(), optimal.value() + 1);

        assertThrows(IllegalArgumentException.class, () -> algo.solve(wrongValue));
    }

    @Test
    void testCopiesKeepInitialSolution() {
        Solution init = emptyKnapsack();

        DdoModel<Integer> model =
                ddoModel()
                        .setInitialSolution(init)
                        .fixWidth(5)
                        .useCache(true)
                        .setCutSetType(CutSetType.Frontier)
                        .disableDominance()
                        .disableLowerBound();

        assertSame(init, model.initialSolution());
    }

    @Test
    void testCopiesKeepReductionSettings() {
        ReductionStrategy<Integer> relax = new CostBased<>(new KSRanking());
        ReductionStrategy<Integer> restrict = new CostBased<>(new KSRanking());
        StateDistance<Integer> distance = (a, b) -> Math.abs(a - b);
        DdoModel<Integer> base = ddoModel();
        DdoModel<Integer> model =
                new DdoModel<>() {
                    @Override
                    public Problem<Integer> problem() {
                        return base.problem();
                    }

                    @Override
                    public Relaxation<Integer> relaxation() {
                        return base.relaxation();
                    }

                    @Override
                    public ReductionStrategy<Integer> relaxStrategy() {
                        return relax;
                    }

                    @Override
                    public ReductionStrategy<Integer> restrictStrategy() {
                        return restrict;
                    }

                    @Override
                    public StateDistance<Integer> stateDistance() {
                        return distance;
                    }
                };

        for (DdoModel<Integer> copy :
                List.of(
                        model.fixWidth(5),
                        model.useCache(true),
                        model.setCutSetType(CutSetType.Frontier),
                        model.disableDominance(),
                        model.disableLowerBound(),
                        model.setInitialSolution(emptyKnapsack()))) {
            assertSame(relax, copy.relaxStrategy());
            assertSame(restrict, copy.restrictStrategy());
            assertSame(distance, copy.stateDistance());
        }
    }

    @Test
    void testFixWidthKeepsLowerBoundDisabled() {
        DdoModel<Integer> model = ddoModel().disableLowerBound().fixWidth(5);

        assertInstanceOf(DefaultFastLowerBound.class, model.lowerBound());
    }
}
