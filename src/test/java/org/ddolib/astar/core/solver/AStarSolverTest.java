package org.ddolib.astar.core.solver;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.common.solver.SearchStatus;
import org.ddolib.common.solver.Solution;
import org.ddolib.examples.gruler.GRProblem;
import org.ddolib.examples.gruler.GRState;
import org.ddolib.examples.knapsack.KSDominance;
import org.ddolib.examples.knapsack.KSFastLowerBound;
import org.ddolib.examples.knapsack.KSProblem;
import org.ddolib.examples.misp.MispProblem;
import org.ddolib.examples.tsp.TSPFastLowerBound;
import org.ddolib.examples.tsp.TSPProblem;
import org.ddolib.examples.tsp.TSPState;
import org.ddolib.modeling.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.BitSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AStarSolverTest {

    @Test
    void testKPGapNonConsistentHeuristic() throws IOException {
        // The knapsack problem is a maximization problem.
        // To turn it into a minimization problem, we model it by minimizing the negative of the profit.
        // Therefore, A-star with an admissible but non-consistent lower-bound (objective value used here) has to continue
        // searching until it can prove that no better solution exists.
        // It can thus not stop at the first found solution.

        final String instance = Path.of("data", "Knapsack", "instance_n100_c500_10_5_10_5_2").toString();
        final KSProblem problem = new KSProblem(instance);
        final Model<Integer> model = new Model<>() {
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
        Solution finalSol = Solvers.minimizeAstar(model, (sol, s) -> {
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
        final int n = 6;
        final GRProblem problem = new GRProblem(n, 17);
        /* @Override
            public FastLowerBound<GRState> lowerBound() {
                return (state, variables) -> 0;
            }*/
        final Model<GRState> model = () -> problem;

        ArrayList<SearchStatistics> statsList = new ArrayList<>();
        Solution finalSol = Solvers.minimizeAstar(model, (sol, s) -> {
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
        // The A-star with an admissible lower-bound can prove that no better solution exists.
        // It can thus stop at the first found solution.
        final String instance = Path.of("data", "TSP", "instance_18_0.xml").toString();
        final TSPProblem problem = new TSPProblem(instance);
        Model<TSPState> model = new Model<>() {
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
        Solution finalSol = Solvers.minimizeAstar(model, (sol, s) -> {
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
    void testMaxProblemWithDefaultLFlb() throws IOException {
        String instance = Path.of("data", "MISP", "weighted.dot").toString();
        final MispProblem problem = new MispProblem(instance);
        Model<BitSet> model = new Model<>() {
            @Override
            public Problem<BitSet> problem() {
                return problem;
            }

            @Override
            public FastLowerBound<BitSet> lowerBound() {
                return new DefaultFastLowerBound<>();
            }
        };

        Solution bestSolution = Solvers.minimizeAstar(model);
        assertEquals(-11.0, bestSolution.value(), 1e-10);
    }
}