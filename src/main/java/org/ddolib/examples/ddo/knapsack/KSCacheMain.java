package org.ddolib.examples.ddo.knapsack;

import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.common.solver.Solver;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

import static org.ddolib.factory.Solvers.sequentialSolverWithCache;

//import static org.ddolib.ddo.implem.solver.Solvers.sequentialSolverWithCache;

/**
 * The Knapsack problem is a classic optimization problem
 * where the goal is to maximize the total value of items
 * while staying within a given weight limit.
 * The dynamic programming model is used to solve this problem is
 * using the recurrence relation:
 * KS(i, c) = max(KS(i-1, c), KS(i-1, c - w[i]) + p[i])
 * where KS(i, c) is the maximum value of the first i items
 * with a knapsack capacity of c, p[i] is the profit of item i,
 * w[i] is the weight of item i.
 */
public class KSCacheMain {
    public static void main(final String[] args) throws IOException {

        final String instance = "data/Knapsack/instance_n1000_c1000_10_5_10_5_9";
        final KSProblem problem = readInstance(instance);
        final KSRelax relax = new KSRelax();
        final KSFastUpperBound fub = new KSFastUpperBound(problem);
        final KSRanking ranking = new KSRanking();
        final FixedWidth<Integer> width = new FixedWidth<>(250);
        final VariableHeuristic<Integer> varh = new DefaultVariableHeuristic<Integer>();
        final SimpleDominanceChecker<Integer, Integer> dominance = new SimpleDominanceChecker<>(new KSDominance(), problem.nbVars());
        final SimpleCache<Integer> cache = new SimpleCache<>();
        final Frontier<Integer> frontier = new SimpleFrontier<>(ranking, CutSetType.LastExactLayer);

        final Solver solver = sequentialSolverWithCache(
                problem,
                relax,
                varh,
                ranking,
                width,
                frontier,
                fub,
                dominance,
                cache,
                100);


        long start = System.currentTimeMillis();
        SearchStatistics stats = solver.maximize();
        double duration = (System.currentTimeMillis() - start) / 1000.0;

        System.out.println("Search statistics:" + stats);


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

    public static KSProblem readInstance(final String fname) throws IOException {
        final File f = new File(fname);
        try (final BufferedReader bf = new BufferedReader(new FileReader(f))) {
            final PinReadContext context = new PinReadContext();

            bf.lines().forEachOrdered((String s) -> {
                if (context.isFirst) {
                    context.isFirst = false;
                    String[] tokens = s.split("\\s");
                    context.n = Integer.parseInt(tokens[0]);
                    context.capa = Integer.parseInt(tokens[1]);

                    if (tokens.length == 3) {
                        context.optimal = Integer.parseInt(tokens[2]);
                    }

                    context.profit = new int[context.n];
                    context.weight = new int[context.n];
                } else {
                    if (context.count < context.n) {
                        String[] tokens = s.split("\\s");
                        context.profit[context.count] = Integer.parseInt(tokens[0]);
                        context.weight[context.count] = Integer.parseInt(tokens[1]);

                        context.count++;
                    }
                }
            });

            return new KSProblem(context.capa, context.profit, context.weight, context.optimal);
        }
    }

    private static class PinReadContext {
        boolean isFirst = true;
        int n = 0;
        int count = 0;
        int capa = 0;
        int[] profit = new int[0];
        int[] weight = new int[0];
        Integer optimal = null;
    }
}
