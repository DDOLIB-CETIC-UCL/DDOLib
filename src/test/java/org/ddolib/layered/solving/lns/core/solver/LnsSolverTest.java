package org.ddolib.layered.solving.lns.core.solver;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.ddolib.common.heuristics.width.FixedWidth;
import org.ddolib.common.heuristics.width.WidthHeuristic;
import org.ddolib.common.solver.stat.SearchStatistics;
import org.ddolib.common.solver.stat.SearchStatus;
import org.ddolib.examples.layered.knapsack.KSDominance;
import org.ddolib.examples.layered.knapsack.KSFastLowerBound;
import org.ddolib.examples.layered.knapsack.KSProblem;
import org.ddolib.examples.layered.knapsack.KSRanking;
import org.ddolib.layered.modeling.DominanceChecker;
import org.ddolib.layered.modeling.FastLowerBound;
import org.ddolib.layered.modeling.LnsModel;
import org.ddolib.layered.modeling.Problem;
import org.ddolib.layered.modeling.SimpleDominanceChecker;
import org.ddolib.layered.modeling.Solvers;
import org.ddolib.layered.solver.Solution;
import org.junit.jupiter.api.Test;

/** Tests of the Large Neighborhood Search (LNS) solver on knapsack instances. */
public class LnsSolverTest {
    @Test
    void testLNSWithoutInitialSolutionOperator() throws IOException {
        final String instance =
                Path.of("data", "Knapsack", "instance_n1000_c1000_10_5_10_5_0").toString();
        final KSProblem problem = new KSProblem(instance);
        final LnsModel<Integer> model =
                new LnsModel<>() {
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
                    public KSRanking ranking() {
                        return new KSRanking();
                    }

                    @Override
                    public WidthHeuristic<Integer> widthHeuristic() {
                        return new FixedWidth<>(10);
                    }
                };

        ArrayList<SearchStatistics> statsList = new ArrayList<>();
        Solvers.minimizeLns(
                model,
                s -> s.runtime() > 1000,
                (sol, s) -> {
                    // verify that each found solution is valid and corresponds to its cost
                    int computedProfit = 0;
                    int computedWeight = 0;

                    for (int i = 0; i < problem.nbVars(); i++) {
                        if (sol[i] == 1) {
                            computedProfit += problem.profit[i];
                            computedWeight += problem.weight[i];
                        }
                    }
                    assertTrue(computedWeight <= problem.capa);
                    assertEquals(-computedProfit, s.incumbent());
                    assertEquals(SearchStatus.SAT, s.status());
                    statsList.add(s);
                });

        assertFalse(statsList.isEmpty());
        for (int i = 1; i < statsList.size(); i++) {
            assertTrue(statsList.get(i).incumbent() < statsList.get(i - 1).incumbent());
            // the gap is computed w.r.t. a global lower bound, it decreases with the incumbent
            assertTrue(statsList.get(i).gap() < statsList.get(i - 1).gap());
            assertTrue(statsList.get(i).nbIterations() > statsList.get(i - 1).nbIterations());
        }
    }

    @Test
    void testLNSFromInitialSolutionOperator() throws IOException {
        final String instance =
                Path.of("data", "Knapsack", "instance_n1000_c1000_10_5_10_5_0").toString();
        final KSProblem problem = new KSProblem(instance);
        final LnsModel<Integer> model =
                new LnsModel<>() {
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
                    public KSRanking ranking() {
                        return new KSRanking();
                    }

                    @Override
                    public WidthHeuristic<Integer> widthHeuristic() {
                        return new FixedWidth<>(10);
                    }

                    @Override
                    public int[] initialSolution() {
                        return new int[] {
                            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                            1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 0, 1,
                            1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0,
                            1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                            1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1
                        };
                    } // incumbent = -17
                };

        ArrayList<SearchStatistics> statsList = new ArrayList<>();
        Solvers.minimizeLns(
                model,
                s -> s.runtime() > 1000,
                (sol, s) -> {
                    // verify that each found solution is valid and corresponds to its cost
                    int computedProfit = 0;
                    int computedWeight = 0;

                    for (int i = 0; i < problem.nbVars(); i++) {
                        if (sol[i] == 1) {
                            computedProfit += problem.profit[i];
                            computedWeight += problem.weight[i];
                        }
                    }
                    assertTrue(computedWeight <= problem.capa);
                    assertEquals(-computedProfit, s.incumbent());
                    assertEquals(SearchStatus.SAT, s.status());
                    statsList.add(s);
                });

        // the initial solution (incumbent = -17) must be improved within the time limit
        assertFalse(statsList.isEmpty());
        for (int i = 1; i < statsList.size(); i++) {
            assertTrue(statsList.get(i).incumbent() < statsList.get(i - 1).incumbent());
            // the gap is computed w.r.t. a global lower bound, it decreases with the incumbent
            assertTrue(statsList.get(i).gap() < statsList.get(i - 1).gap());
            assertTrue(statsList.get(i).nbIterations() > statsList.get(i - 1).nbIterations());
        }
    }

    @Test
    void testGapIsWellComputedWhenIncumbentIsZero() {
        final KSProblem problem = new KSProblem(10, new int[] {0, 0, 0}, new int[] {2, 3, 4});
        final LnsModel<Integer> model =
                new LnsModel<>() {
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
                    public KSRanking ranking() {
                        return new KSRanking();
                    }

                    @Override
                    public WidthHeuristic<Integer> widthHeuristic() {
                        return new FixedWidth<>(2);
                    }

                    @Override
                    public int[] initialSolution() {
                        return new int[problem.nbVars()];
                    }
                };

        Solution finalSol = Solvers.minimizeLns(model, s -> s.nbIterations() > 1, (sol, s) -> {});

        assertEquals(0.0, finalSol.statistics().incumbent());
        assertFalse(Double.isNaN(finalSol.statistics().gap()));
        assertFalse(Double.isInfinite(finalSol.statistics().gap()));
        assertEquals(0.0, finalSol.statistics().gap(), 1e-12);
        // the initial solution is never improved: it must be returned as the best solution
        assertArrayEquals(new int[problem.nbVars()], finalSol.solution());
    }

    private static LnsModel<Integer> ksModel(KSProblem problem, int width) {
        return new LnsModel<>() {
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
            public KSRanking ranking() {
                return new KSRanking();
            }

            @Override
            public WidthHeuristic<Integer> widthHeuristic() {
                return new FixedWidth<>(width);
            }
        };
    }

    @Test
    void testOptimalityIsOnlyProvedFromTheRoot() {
        // item 0 alone is optimal (profit 10) but the initial solution takes items 1 and 2 (profit
        // 2)
        final KSProblem problem = new KSProblem(10, new int[] {10, 1, 1}, new int[] {10, 5, 5});
        final LnsModel<Integer> model =
                ksModel(problem, 100).setInitialSolution(new int[] {0, 1, 1});

        // The first neighborhood fixes x0 = 0: its DD is exact but it does not contain the
        // optimum, the solver must not conclude that the incumbent is optimal.
        Solution sol = Solvers.minimizeLns(model, s -> s.nbIterations() > 20);

        assertEquals(-10.0, sol.value());
        assertEquals(SearchStatus.OPTIMAL, sol.statistics().status());
        assertEquals(0.0, sol.statistics().gap(), 1e-12);
    }

    private static List<String> runTrace(LnsModel<Integer> model) {
        List<String> trace = new ArrayList<>();
        Solvers.minimizeLns(
                model,
                s -> s.nbIterations() > 300,
                (sol, s) -> trace.add(s.nbIterations() + ":" + s.incumbent()));
        return trace;
    }

    @Test
    void testSameSeedGivesSameSearch() throws IOException {
        final String instance =
                Path.of("data", "Knapsack", "instance_n1000_c1000_10_5_10_5_0").toString();
        final KSProblem problem = new KSProblem(instance);
        final LnsModel<Integer> model = ksModel(problem, 10).setSeed(42);

        List<String> first = runTrace(model);
        List<String> second = runTrace(model);

        assertFalse(first.isEmpty());
        assertEquals(first, second);
    }

    @Test
    void testModelCopiesKeepLnsParameters() {
        final KSProblem problem = new KSProblem(10, new int[] {1, 2, 3}, new int[] {2, 3, 4});
        final int[] initial = new int[] {1, 0, 0};
        final LnsModel<Integer> model =
                ksModel(problem, 10)
                        .setSeed(7)
                        .setInitialSolution(initial)
                        .setProbability(0.3)
                        .fixWidth(20);

        assertEquals(7, model.seed());
        assertEquals(0.3, model.probability());
        assertArrayEquals(initial, model.initialSolution());
        assertEquals(LnsModel.DEFAULT_SEED, ksModel(problem, 10).seed());
    }
}
