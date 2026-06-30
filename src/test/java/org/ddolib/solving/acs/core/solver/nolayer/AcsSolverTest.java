package org.ddolib.solving.acs.core.solver.nolayer;

import org.ddolib.common.solver.layered.Solution;
import org.ddolib.common.solver.stat.SearchStatistics;
import org.ddolib.common.solver.stat.SearchStatus;
import org.ddolib.examples.nolayer.gruler.GRProblem;
import org.ddolib.examples.nolayer.gruler.GRState;
import org.ddolib.examples.nolayer.knapsack.KSModel;
import org.ddolib.examples.nolayer.knapsack.KSProblem;
import org.ddolib.examples.nolayer.knapsack.KSState;
import org.ddolib.examples.nolayer.tsp.TSPModel;
import org.ddolib.examples.nolayer.tsp.TSPProblem;
import org.ddolib.examples.nolayer.tsp.TSPState;
import org.ddolib.modeling.nolayer.AcsModel;
import org.ddolib.modeling.nolayer.FastLowerBound;
import org.ddolib.modeling.nolayer.Problem;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AcsSolverTest {
    @Test
    void testKPGapNonConsistentHeuristic() throws IOException {
        final String instance = Path.of("data", "Knapsack", "instance_n100_c500_10_5_10_5_2").toString();
        final KSProblem problem = KSProblem.fromFile(instance);
        final KSModel baseModel = new KSModel(problem);

        final AcsModel<KSState> model = new AcsModel<>() {
            @Override
            public Problem<KSState> problem() {
                return problem;
            }

            @Override
            public FastLowerBound<KSState> lowerBound() {
                return baseModel.lowerBound();
            }
        };

        ArrayList<SearchStatistics> statsList = new ArrayList<>();
        Solution finalSol = org.ddolib.modeling.nolayer.Solvers.minimizeAcs(model, (sol, s) -> {
            int computedProfit = 0;
            int computedWeight = 0;
            for (int i = 0; i < problem.profit.length; i++) {
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

        for (int i = 1; i < statsList.size(); i++) {
            assertTrue(statsList.get(i).incumbent() < statsList.get(i - 1).incumbent());
            assertTrue(statsList.get(i).gap() < statsList.get(i - 1).gap());
            assertTrue(statsList.get(i).nbIterations() > statsList.get(i - 1).nbIterations());
        }

        assertEquals(0.0, finalSol.statistics().gap());
        assertEquals(SearchStatus.OPTIMAL, finalSol.statistics().status());
    }

    @Test
    void testGRGapNonConsistentHeuristic() {
        final int n = 7;
        final GRProblem problem = new GRProblem(n);
        final AcsModel<GRState> model = () -> problem;

        ArrayList<SearchStatistics> statsList = new ArrayList<>();
        Solution finalSol = org.ddolib.modeling.nolayer.Solvers.minimizeAcs(model, (sol, s) -> {
            assertEquals(n - 1, sol.length);
            assertEquals(sol[n - 2], s.incumbent());
            assertEquals(SearchStatus.SAT, s.status());
            statsList.add(s);
        });

        for (int i = 1; i < statsList.size(); i++) {
            assertTrue(statsList.get(i).incumbent() < statsList.get(i - 1).incumbent());
            assertTrue(statsList.get(i).gap() < statsList.get(i - 1).gap());
            assertTrue(statsList.get(i).nbIterations() > statsList.get(i - 1).nbIterations());
        }

        assertEquals(0.0, finalSol.statistics().gap());
        assertEquals(SearchStatus.OPTIMAL, finalSol.statistics().status());
    }

    @Test
    void testTSPGapNonConsistentHeuristic() throws IOException {
        final String instance = Path.of("data", "TSP", "instance_10_0.xml").toString();
        final org.ddolib.examples.layered.tsp.TSPProblem baseTSP = new org.ddolib.examples.layered.tsp.TSPProblem(instance);
        final TSPProblem problem = new TSPProblem(baseTSP.distanceMatrix);
        final TSPModel baseModel = new TSPModel(problem);

        final AcsModel<TSPState> model = new AcsModel<>() {
            @Override
            public Problem<TSPState> problem() {
                return problem;
            }

            @Override
            public FastLowerBound<TSPState> lowerBound() {
                return baseModel.lowerBound();
            }
        };

        ArrayList<SearchStatistics> statsList = new ArrayList<>();
        Solution finalSol = org.ddolib.modeling.nolayer.Solvers.minimizeAcs(model, (sol, s) -> {
            double computedCost = 0;
            int current = 0;
            int n = problem.distanceMatrix.length;
            for (int i = 0; i < n - 1; i++) {
                computedCost += problem.distanceMatrix[current][sol[i]];
                current = sol[i];
            }
            computedCost += problem.distanceMatrix[current][0];

            assertEquals(n - 1, sol.length);
            assertEquals(computedCost, s.incumbent());
            assertEquals(SearchStatus.SAT, s.status());
            statsList.add(s);
        });

        for (int i = 1; i < statsList.size(); i++) {
            assertTrue(statsList.get(i).incumbent() < statsList.get(i - 1).incumbent());
            assertTrue(statsList.get(i).gap() < statsList.get(i - 1).gap());
            assertTrue(statsList.get(i).nbIterations() > statsList.get(i - 1).nbIterations());
        }

        assertEquals(0.0, finalSol.statistics().gap());
        assertEquals(SearchStatus.OPTIMAL, finalSol.statistics().status());
    }
}
