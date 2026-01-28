package org.ddolib.acs.core.solver;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.common.solver.SearchStatus;
import org.ddolib.common.solver.Solution;
import org.ddolib.examples.boundedknapsack.BKSDominance;
import org.ddolib.examples.boundedknapsack.BKSFastLowerBound;
import org.ddolib.examples.boundedknapsack.BKSProblem;
import org.ddolib.examples.gruler.GRProblem;
import org.ddolib.examples.gruler.GRState;
import org.ddolib.examples.knapsack.KSDominance;
import org.ddolib.examples.knapsack.KSFastLowerBound;
import org.ddolib.examples.knapsack.KSProblem;
import org.ddolib.examples.tsp.TSPFastLowerBound;
import org.ddolib.examples.tsp.TSPProblem;
import org.ddolib.examples.tsp.TSPState;
import org.ddolib.modeling.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AcsSolverTest {
    @Test
    void testKPGapNonConsistentHeuristic() throws IOException {
        // The knapsack problem is a maximization problem.
        // To turn it into a minimization problem, we model it by minimizing the negative of the profit.
        // Therefore, Acs with a lower-bound (objective value used here) can be interrupted at any
        // time while providing a relevant and improved solution over time.
        // It can prove that no better solution exists.
        final String instance = Path.of("data", "Knapsack", "instance_n100_c500_10_5_10_5_2").toString();
        final KSProblem problem = new KSProblem(instance);
        final AcsModel<Integer> model = new AcsModel<>() {
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
        };
        ArrayList<SearchStatistics> statsList = new ArrayList<>();
        Solution finalSol = Solvers.minimizeAcs(model, (sol, s) -> {
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

        // verify that the solutions are improving and the gap is decreasing
        for (int i = 1; i < statsList.size(); i++) {
            assertTrue(statsList.get(i).incumbent() < statsList.get(i - 1).incumbent());
            assertTrue(statsList.get(i).gap() < statsList.get(i - 1).gap());
            assertTrue(statsList.get(i).nbIterations() > statsList.get(i - 1).nbIterations());
        }

        // final solution, gap should be zero
        assertEquals(0.0, finalSol.statistics().gap());
        assertEquals(SearchStatus.OPTIMAL, finalSol.statistics().status());
    }

    @Test
    void testGRGapNonConsistentHeuristic() throws IOException {
        // The golomb ruler problem is a minimization problem.
        // Ddo is used to improved solution over time
        // and prove that no better solution exists.
        final int n = 7;
        final GRProblem problem = new GRProblem(n);
        final AcsModel<GRState> model = () -> problem;

        ArrayList<SearchStatistics> statsList = new ArrayList<>();
        Solution finalSol = Solvers.minimizeAcs(model, (sol, s) -> {
            // verify that each found solution is valid
            assertEquals(n - 1, sol.length);
            assertEquals(sol[n - 2], s.incumbent());
            assertEquals(SearchStatus.SAT, s.status());
            statsList.add(s);
        });
        // verify that the solutions are improving and the gap is decreasing
        for (int i = 1; i < statsList.size(); i++) {
            assertTrue(statsList.get(i).incumbent() < statsList.get(i - 1).incumbent());
            assertTrue(statsList.get(i).gap() < statsList.get(i - 1).gap());
            assertTrue(statsList.get(i).nbIterations() > statsList.get(i - 1).nbIterations());
        }


        // final solution, gap should be zero
        assertEquals(0.0, finalSol.statistics().gap());
        assertEquals(SearchStatus.OPTIMAL, finalSol.statistics().status());
    }

    @Test
    void testTSPGapNonConsistentHeuristic() throws IOException {
        // The TSP problem is a minimization problem.
        // The Acs with a lower-bound (objective value used here) can be interrupted at any
        // time while providing a relevant and improved solution over time.
        // It can prove that no better solution exists.
        final String instance = Path.of("data", "TSP", "instance_10_0.xml").toString();
        final TSPProblem problem = new TSPProblem(instance);
        AcsModel<TSPState> model = new AcsModel<TSPState>() {
            @Override
            public Problem<TSPState> problem() {
                return problem;
            }

            @Override
            public TSPFastLowerBound lowerBound() {
                return new TSPFastLowerBound(problem);
            }
        };
        ArrayList<SearchStatistics> statsList = new ArrayList<>();
        Solution finalSol = Solvers.minimizeAcs(model, (sol, s) -> {
            // verify that each found solution is valid and corresponds to its cost
            double computedCost = problem.eval(sol) + problem.distanceMatrix[0][sol[0]];
            assertEquals(problem.nbVars(), sol.length);
            assertEquals(computedCost, s.incumbent());
            assertEquals(SearchStatus.SAT, s.status());
            statsList.add(s);
        });

        // verify that the solutions are improving and the gap is decreasing
        for (int i = 1; i < statsList.size(); i++) {
            assertTrue(statsList.get(i).incumbent() < statsList.get(i - 1).incumbent());
            assertTrue(statsList.get(i).gap() < statsList.get(i - 1).gap());
            assertTrue(statsList.get(i).nbIterations() > statsList.get(i - 1).nbIterations());
        }

        // final solution, gap should be zero
        assertEquals(0.0, finalSol.statistics().gap());
        assertEquals(SearchStatus.OPTIMAL, finalSol.statistics().status());
    }

    @Test
    void testBKSGapNonConsistentHeuristic() throws IOException, InvalidSolutionException {
        // The BKS problem is a maximization problem.
        // The Acs with a lower-bound (objective value used here) can be interrupted at any
        // time while providing a relevant and improved solution over time.
        // It can prove that no better solution exists.
        final BKSProblem problem = new BKSProblem(10, 1000, BKSProblem.InstanceType.STRONGLY_CORRELATED, 0);
        AcsModel<Integer> model = new AcsModel<>() {
            @Override
            public BKSProblem problem() {
                return problem;
            }

            @Override
            public BKSFastLowerBound lowerBound() {
                return new BKSFastLowerBound(problem);
            }

            @Override
            public DominanceChecker<Integer> dominance() {
                return new SimpleDominanceChecker<>(new BKSDominance(), problem.nbVars());
            }
        };
        ArrayList<SearchStatistics> statsList = new ArrayList<>();
        Solution finalSol = Solvers.minimizeAcs(model, (sol, s) -> {
            // verify that each found solution is valid and corresponds to its cost
            try {
                double computedCost = problem.evaluate(sol);
                assertEquals(computedCost, s.incumbent());
            } catch (InvalidSolutionException e) {
                throw new RuntimeException(e);
            }
            assertEquals(problem.nbVars(), sol.length);
            assertEquals(SearchStatus.SAT, s.status());
            statsList.add(s);
        });

        // verify that the solutions are improving and the gap is decreasing
        for (int i = 1; i < statsList.size(); i++) {
            assertTrue(statsList.get(i).incumbent() < statsList.get(i - 1).incumbent());
            assertTrue(statsList.get(i).gap() < statsList.get(i - 1).gap());
            assertTrue(statsList.get(i).nbIterations() > statsList.get(i - 1).nbIterations());
        }

        // final solution, gap should be zero
        assertEquals(0.0, finalSol.statistics().gap());
        assertEquals(SearchStatus.OPTIMAL, finalSol.statistics().status());
    }
}
