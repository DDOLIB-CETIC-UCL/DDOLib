package org.ddolib.nolayer.solver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import org.ddolib.common.solver.stat.AstarStats;
import org.ddolib.common.solver.stat.SearchStatistics;
import org.ddolib.common.solver.stat.SearchStatus;
import org.ddolib.common.util.debug.DebugLevel;
import org.ddolib.examples.nolayer.knapsack.KSFlb;
import org.ddolib.examples.nolayer.knapsack.KSProblem;
import org.ddolib.examples.nolayer.knapsack.KSState;
import org.ddolib.layered.modeling.StateRanking;
import org.ddolib.nolayer.modeling.AcsModel;
import org.ddolib.nolayer.modeling.AwAstarModel;
import org.ddolib.nolayer.modeling.DdoModel;
import org.ddolib.nolayer.modeling.FastLowerBound;
import org.ddolib.nolayer.modeling.Model;
import org.ddolib.nolayer.modeling.Problem;
import org.ddolib.nolayer.modeling.Relaxation;
import org.ddolib.nolayer.modeling.Solvers;
import org.ddolib.nolayer.solving.ddo.core.heuristics.cluster.CostBased;
import org.ddolib.nolayer.solving.ddo.core.heuristics.cluster.ReductionStrategy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * Tests of the initial solution given to the no-layer solvers through {@link
 * Model#initialSolution()}.
 */
class InitialSolutionTest {

    private static final int[] PROFITS = {10, 7, 8, 4, 6, 3, 9, 5, 2, 6};

    private static final KSProblem PROBLEM =
            new KSProblem(PROFITS, new int[] {5, 4, 6, 3, 5, 2, 7, 4, 1, 4}, 20);

    /** Solvers able to start from an initial solution. */
    private enum Algo {
        ASTAR {
            @Override
            Solution solve(
                    Solution init,
                    Predicate<SearchStatistics> limit,
                    BiConsumer<List<Integer>, SearchStatistics> onSolution) {
                return Solvers.minimizeAstar(
                        baseModel().setInitialSolution(init), limit, onSolution);
            }
        },
        AWASTAR {
            @Override
            Solution solve(
                    Solution init,
                    Predicate<SearchStatistics> limit,
                    BiConsumer<List<Integer>, SearchStatistics> onSolution) {
                AwAstarModel<KSState> model =
                        new AwAstarModel<>() {
                            @Override
                            public Problem<KSState> problem() {
                                return PROBLEM;
                            }

                            @Override
                            public FastLowerBound<KSState> lowerBound() {
                                return new KSFlb(PROBLEM);
                            }

                            @Override
                            public DebugLevel debugMode() {
                                return DebugLevel.ON;
                            }
                        };
                return Solvers.minimizeAwAstar(model.setInitialSolution(init), limit, onSolution);
            }
        },
        ACS {
            @Override
            Solution solve(
                    Solution init,
                    Predicate<SearchStatistics> limit,
                    BiConsumer<List<Integer>, SearchStatistics> onSolution) {
                return Solvers.minimizeAcs(acsModel().setInitialSolution(init), limit, onSolution);
            }
        },
        DDO {
            @Override
            Solution solve(
                    Solution init,
                    Predicate<SearchStatistics> limit,
                    BiConsumer<List<Integer>, SearchStatistics> onSolution) {
                return Solvers.minimizeDdo(ddoModel().setInitialSolution(init), limit, onSolution);
            }
        };

        abstract Solution solve(
                Solution init,
                Predicate<SearchStatistics> limit,
                BiConsumer<List<Integer>, SearchStatistics> onSolution);

        Solution solve(Solution init) {
            return solve(init, s -> false, (sol, s) -> {});
        }
    }

    private static Model<KSState> baseModel() {
        return new Model<>() {
            @Override
            public Problem<KSState> problem() {
                return PROBLEM;
            }

            @Override
            public FastLowerBound<KSState> lowerBound() {
                return new KSFlb(PROBLEM);
            }

            @Override
            public DebugLevel debugMode() {
                return DebugLevel.ON;
            }
        };
    }

    private static AcsModel<KSState> acsModel() {
        return new AcsModel<>() {
            @Override
            public Problem<KSState> problem() {
                return PROBLEM;
            }

            @Override
            public FastLowerBound<KSState> lowerBound() {
                return new KSFlb(PROBLEM);
            }

            @Override
            public DebugLevel debugMode() {
                return DebugLevel.ON;
            }
        };
    }

    private static DdoModel<KSState> ddoModel() {
        return new DdoModel<>() {
            @Override
            public Problem<KSState> problem() {
                return PROBLEM;
            }

            @Override
            public FastLowerBound<KSState> lowerBound() {
                return new KSFlb(PROBLEM);
            }

            @Override
            public DebugLevel debugMode() {
                return DebugLevel.ON;
            }

            @Override
            public Relaxation<KSState> relaxation() {
                return states -> {
                    Iterator<KSState> it = states.iterator();
                    KSState first = it.next();
                    int maxCap = first.remainingCapacity();
                    int maxItem = first.currentItem();
                    while (it.hasNext()) {
                        KSState s = it.next();
                        maxCap = Math.max(maxCap, s.remainingCapacity());
                        maxItem = Math.max(maxItem, s.currentItem());
                    }
                    return new KSState(maxItem, maxCap);
                };
            }

            @Override
            public StateRanking<KSState> ranking() {
                return (s1, s2) -> Integer.compare(s2.remainingCapacity(), s1.remainingCapacity());
            }
        };
    }

    private static Solution optimalSolution() {
        return Solvers.minimizeAstar(baseModel());
    }

    /** Solution taking no item, of value 0. */
    private static Solution emptyKnapsack() {
        return new Solution(Collections.nCopies(PROFITS.length, 0), new AstarStats(0, 0));
    }

    @ParameterizedTest
    @EnumSource(Algo.class)
    void testOptimalInitialSolutionIsReturned(Algo algo) {
        Solution optimal = optimalSolution();
        AtomicInteger nbCallbacks = new AtomicInteger();

        Solution sol = algo.solve(optimal, s -> false, (x, s) -> nbCallbacks.incrementAndGet());

        assertFalse(sol.isEmpty());
        assertEquals(optimal.value(), sol.value(), 1e-10);
        assertEquals(optimal.solution(), sol.solution());
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

        assertEquals(init.solution(), sol.solution());
        assertEquals(init.value(), sol.value(), 1e-10);
        assertEquals(SearchStatus.SAT, sol.statistics().status());
    }

    @ParameterizedTest
    @EnumSource(Algo.class)
    void testEmptyInitialSolutionIsIgnored(Algo algo) {
        Solution noSolution = new Solution(List.of(), new AstarStats(0, 0));

        Solution sol = algo.solve(noSolution);

        assertEquals(optimalSolution().value(), sol.value(), 1e-10);
    }

    @ParameterizedTest
    @EnumSource(Algo.class)
    void testInvalidValueRejectedInDebugMode(Algo algo) {
        Solution optimal = optimalSolution();
        Solution wrongValue =
                new Solution(optimal.solution(), new AstarStats(0, optimal.value() + 1));

        assertThrows(IllegalArgumentException.class, () -> algo.solve(wrongValue));
    }

    @Test
    void testCopiesKeepInitialSolution() {
        Solution init = emptyKnapsack();

        assertSame(init, acsModel().setInitialSolution(init).setColumnWidth(8).initialSolution());

        AwAstarModel<KSState> awModel = () -> PROBLEM;
        assertSame(init, awModel.setInitialSolution(init).setWeight(3).initialSolution());
    }

    @Test
    void testCopiesKeepOtherSettings() {
        FastLowerBound<KSState> lb = new KSFlb(PROBLEM);
        ReductionStrategy<KSState> relax = new CostBased<>((a, b) -> 0);
        ReductionStrategy<KSState> restrict = new CostBased<>((a, b) -> 0);
        DdoModel<KSState> base = ddoModel();
        DdoModel<KSState> model =
                new DdoModel<>() {
                    @Override
                    public Problem<KSState> problem() {
                        return PROBLEM;
                    }

                    @Override
                    public FastLowerBound<KSState> lowerBound() {
                        return lb;
                    }

                    @Override
                    public Relaxation<KSState> relaxation() {
                        return base.relaxation();
                    }

                    @Override
                    public ReductionStrategy<KSState> relaxStrategy() {
                        return relax;
                    }

                    @Override
                    public ReductionStrategy<KSState> restrictStrategy() {
                        return restrict;
                    }

                    @Override
                    public boolean useCache() {
                        return true;
                    }
                };

        DdoModel<KSState> copy = model.setInitialSolution(emptyKnapsack());

        assertSame(lb, copy.lowerBound());
        assertSame(relax, copy.relaxStrategy());
        assertSame(restrict, copy.restrictStrategy());
        assertEquals(true, copy.useCache());

        AwAstarModel<KSState> awModel =
                new AwAstarModel<>() {
                    @Override
                    public Problem<KSState> problem() {
                        return PROBLEM;
                    }

                    @Override
                    public double weight() {
                        return 7;
                    }
                };
        assertEquals(7, awModel.setInitialSolution(emptyKnapsack()).weight());
    }
}
