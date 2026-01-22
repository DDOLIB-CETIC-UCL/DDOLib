package org.ddolib.awastar.core.solver;

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
import org.ddolib.examples.tsp.TSPFastLowerBound;
import org.ddolib.examples.tsp.TSPProblem;
import org.ddolib.examples.tsp.TSPState;
import org.ddolib.modeling.AwAstarModel;
import org.ddolib.modeling.FastLowerBound;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solvers;
import org.ddolib.util.io.SolutionPrinter;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AwAStarSolverTest {


    @Test
    void testKP() throws IOException {
        final String instance = Path.of("data", "Knapsack", "instance_n100_c500_10_5_10_5_2").toString();
        final KSProblem problem = new KSProblem(instance);
        final AwAstarModel<Integer> model = new AwAstarModel<>() {
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
        Solution finalSol = Solvers.minimizeAwAStar(model, (sol, s) -> {
            SolutionPrinter.printSolution(s, sol);
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
    void testGR() {
        final int n = 7;
        final GRProblem problem = new GRProblem(n, 25);
        final AwAstarModel<GRState> model = new AwAstarModel<>() {
            @Override
            public Problem<GRState> problem() {
                return problem;
            }

            @Override
            public FastLowerBound<GRState> lowerBound() {
                return (state, variables) -> 0;
            }
        };

        ArrayList<SearchStatistics> statsList = new ArrayList<>();
        Solution finalSol = Solvers.minimizeAwAStar(model, (sol, s) -> {
            SolutionPrinter.printSolution(s, sol);
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
    void testTSP() throws IOException {
        final String instance = Path.of("data", "TSP", "instance_18_0.xml").toString();
        final TSPProblem problem = new TSPProblem(instance);
        AwAstarModel<TSPState> model = new AwAstarModel<>() {
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
        Solution finalSol = Solvers.minimizeAwAStar(model, (sol, s) -> {
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

        System.out.println(finalSol);

        // final solution, gap should be zero
        assertEquals(0.0, finalSol.statistics().gap());
        assertEquals(SearchStatus.OPTIMAL, finalSol.statistics().status());
    }


}
