package org.ddolib.astar.core.solver;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.common.solver.SearchStatus;
import org.ddolib.examples.knapsack.KSDominance;
import org.ddolib.examples.knapsack.KSFastLowerBound;
import org.ddolib.examples.knapsack.KSProblem;
import org.ddolib.modeling.FastLowerBound;
import org.ddolib.modeling.Model;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solvers;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AStarSolverTest {

    @Test
    void testGapNonConsistentHeuristic() throws IOException {
        // The knapsack problem is a maximization problem.
        // To turn it into a minimization problem, we model it by minimizing the negative of the profit.
        // Therefore, A-star with an admissible but non-consistent lower-bound (objective value used here) has to continue
        // searching until it can prove that no better solution exists.
        // It can thus not stop at the first found solution.

        final String instance = Path.of("data","Knapsack","instance_n100_c500_10_5_10_5_2").toString();
        final KSProblem problem = new KSProblem(instance);
        final Model<Integer> model = new Model<>() {
            @Override
            public Problem<Integer> problem() {
                return problem;
            }
            @Override
            public DominanceChecker<Integer> dominance() {
                return new SimpleDominanceChecker<>(new KSDominance(), problem.nbVars());
            }
            @Override
            public FastLowerBound<Integer> lowerBound() {
                return new FastLowerBound<Integer>() {
                    @Override
                    public double fastLowerBound(Integer state, Set<Integer> variables) {
                        return problem.optimalValue().get();
                    }
                };
            }
        };

        ArrayList<SearchStatistics> statsList = new ArrayList<>();
        SearchStatistics finalStats = Solvers.minimizeAstar(model, (sol, s) -> {
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
            assertEquals(SearchStatus.UNKNOWN, s.status());
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