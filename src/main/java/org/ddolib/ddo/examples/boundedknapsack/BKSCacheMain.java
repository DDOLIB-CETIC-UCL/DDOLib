package org.ddolib.ddo.examples.boundedknapsack;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.cache.SimpleCache;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.Frontier;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.ddo.core.heuristics.variable.VariableHeuristic;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.profiling.SearchStatistics;
import org.ddolib.ddo.core.solver.SequentialSolverWithCache;
import org.ddolib.dominance.DominanceChecker;
import org.ddolib.dominance.SimpleDominanceChecker;
import org.ddolib.solver.Solver;

import java.util.Arrays;

/**
 * Bounded Knapsack Problem (BKS)
 * A bounded knapsack problem is a variation of the classic knapsack problem
 * where each item can be included in the knapsack a limited number of times.
 */
public class BKSCacheMain {

    public static void main(String[] args) {
        // Example from the paper "Decision Diagram-Based Branch and Bound with Caching"
        final BKSProblem problem = new BKSProblem(15, // capacity
                new int[]{2, 3, 6, 6, 1}, // values
                new int[]{4, 6, 4, 2, 5},  // weights
                new int[]{1, 1, 2, 2, 1}); // number of items
        final BKSRelax relax = new BKSRelax();
        final BKSFastUpperBound fub = new BKSFastUpperBound(problem);
        final BKSRanking ranking = new BKSRanking();
        final FixedWidth<Integer> width = new FixedWidth<>(3);
        final VariableHeuristic<Integer> varh = new DefaultVariableHeuristic<Integer>();
        final DominanceChecker<Integer, Integer> dominance =
                new SimpleDominanceChecker<>(new BKSDominance(),
                        problem.nbVars());
        final SimpleCache<Integer> cache = new SimpleCache<>();
        final Frontier<Integer> frontier = new SimpleFrontier<>(ranking, CutSetType.Frontier);

        final Solver solver = new SequentialSolverWithCache<>(
                problem,
                relax,
                varh,
                ranking,
                width, frontier,
                fub,
                dominance,
                cache);


        long start = System.currentTimeMillis();
        SearchStatistics stats = solver.maximize();
        double duration = (System.currentTimeMillis() - start) / 1000.0;
        System.out.println(stats);

        int[] solution = solver.bestSolution().map(decisions -> {
            int[] values = new int[problem.nbVars()];
            for (Decision d : decisions) {
                values[d.var()] = d.val();
            }
            return values;
        }).get();

        System.out.printf("Duration : %.3f seconds%n", duration);
        System.out.printf("Objective: %s%n", solver.bestValue().get());
        System.out.printf("Solution : %s%n", Arrays.toString(solution));
    }
}
