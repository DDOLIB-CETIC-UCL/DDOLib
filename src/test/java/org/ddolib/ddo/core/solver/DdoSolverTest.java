package org.ddolib.ddo.core.solver;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.common.solver.SearchStatus;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.examples.gruler.GRProblem;
import org.ddolib.examples.gruler.GRRanking;
import org.ddolib.examples.gruler.GRRelax;
import org.ddolib.examples.gruler.GRState;
import org.ddolib.examples.knapsack.*;
import org.ddolib.examples.tsp.*;
import org.ddolib.modeling.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DdoSolverTest {
    @Test
    void testKPGapNonConsistentHeuristic() throws IOException {
        // The knapsack problem is a maximization problem.
        // To turn it into a minimization problem, we model it by minimizing the negative of the profit.
        // Therefore, Ddo with a lower-bound (objective value used here) can improved solution over time
        // and prove that no better solution exists.
        final String instance = Path.of("data","Knapsack","instance_n100_c500_10_5_10_5_2").toString();
        final KSProblem problem = new KSProblem(instance);
        final DdoModel<Integer> model = new DdoModel<>() {
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
                return true;
            }
        };

        ArrayList<SearchStatistics> statsList = new ArrayList<>();
        SearchStatistics finalStats = Solvers.minimizeDdo(model, (sol, s) -> {
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
        assertEquals(0.0, finalStats.gap());
        assertEquals(SearchStatus.OPTIMAL, finalStats.status());
    }

    @Test
    void testGRGapNonConsistentHeuristic() throws IOException {
        // The golomb ruler problem is a minimization problem.
        // Ddo is used to improved solution over time
        // and prove that no better solution exists.
        final int n = 7;
        final GRProblem problem = new GRProblem(n);
        final DdoModel<GRState> model = new DdoModel<>() {
            @Override
            public Problem<GRState> problem() {
                return problem;
            }

            @Override
            public Relaxation<GRState> relaxation() {
                return new GRRelax();
            }

            @Override
            public GRRanking ranking() {
                return new GRRanking();
            }
        };

        ArrayList<SearchStatistics> statsList = new ArrayList<>();
        SearchStatistics finalStats = Solvers.minimizeDdo(model, (sol, s) -> {
            // verify that each found solution is valid
            assertEquals(n - 1, sol.length);
            assertEquals(sol[n-2], s.incumbent());
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
        assertEquals(0.0, finalStats.gap());
        assertEquals(SearchStatus.OPTIMAL, finalStats.status());
    }

    @Test
    void testTSPGapNonConsistentHeuristic() throws IOException {
        // The TSP problem is a minimization problem.
        // The Ddo with a lower-bound (objective value used here) can improved solution over time
        // and prove that no better solution exists.
        final String instance = Path.of("data", "TSP", "instance_18_0.xml").toString();
        final TSPProblem problem = new TSPProblem(instance);
        DdoModel<TSPState> model = new DdoModel<TSPState>() {
            @Override
            public Problem<TSPState> problem() {
                return problem;
            }

            @Override
            public Relaxation<TSPState> relaxation() {
                return new TSPRelax(problem);
            }

            @Override
            public TSPRanking ranking() {
                return new TSPRanking();
            }

            @Override
            public TSPFastLowerBound lowerBound() {
                return new TSPFastLowerBound(problem);
            }

            @Override
            public boolean useCache() {
                return true;
            }

            @Override
            public WidthHeuristic<TSPState> widthHeuristic() {
                return new FixedWidth<>(500);
            }
        };

        ArrayList<SearchStatistics> statsList = new ArrayList<>();
        SearchStatistics finalStats = Solvers.minimizeDdo(model, (sol, s) -> {
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
        assertEquals(0.0, finalStats.gap());
        assertEquals(SearchStatus.OPTIMAL, finalStats.status());
    }
}
