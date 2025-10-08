package org.ddolib.examples.boundedknapsack;

import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.common.solver.Solver;
import org.ddolib.common.solver.SolverConfig;
import org.ddolib.ddo.core.cache.SimpleCache;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.profiling.SearchStatistics;
import org.ddolib.ddo.core.solver.SequentialSolver;

import java.util.Arrays;

/**
 * Bounded Knapsack Problem (BKS)
 * A bounded knapsack problem is a variation of the classic knapsack problem
 * where each item can be included in the knapsack a limited number of times.
 */
public class BKSMain {

    public static void main(String[] args) {
        // Example from the paper "Decision Diagram-Based Branch and Bound with Caching"
        SolverConfig<Integer, Integer> config = new SolverConfig<>();

        final BKSProblem problem = new BKSProblem(15, // capacity
                new int[]{2, 3, 6, 6, 1}, // values
                new int[]{4, 6, 4, 2, 5},  // weights
                new int[]{1, 1, 2, 2, 1}); // number of items

        config.problem = problem;

        config.relax = new BKSRelax();
        config.flb = new BKSFastLowerBound(problem);
        config.ranking = new BKSRanking();
        config.width = new FixedWidth<>(3);
        config.varh = new DefaultVariableHeuristic<>();
        config.dominance = new SimpleDominanceChecker<>(new BKSDominance(), problem.nbVars());
        config.cache = new SimpleCache<>();
        config.frontier = new SimpleFrontier<>(config.ranking, CutSetType.Frontier);

        final Solver solver = new SequentialSolver<>(config);


        long start = System.currentTimeMillis();
        SearchStatistics stats = solver.minimize();
        double duration = (System.currentTimeMillis() - start) / 1000.0;
        System.out.println(stats);

        int[] solution = solver.constructBestSolution(problem.nbVars());

        System.out.printf("Duration : %.3f seconds%n", duration);
        System.out.printf("Objective: %s%n", solver.bestValue().get());
        System.out.printf("Solution : %s%n", Arrays.toString(solution));
    }
}
