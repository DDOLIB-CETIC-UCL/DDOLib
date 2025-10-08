package org.ddolib.examples.knapsack;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.ddo.core.profiling.SearchStatistics;
import org.ddolib.modeling.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

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
public class KSAcsMain {
    public static void main(final String[] args) throws IOException {
        final String instance = "data/Knapsack/instance_n1000_c1000_10_5_10_5_0";
        final KSProblem problem = readInstance(instance);

        final AcsModel<Integer> model = new AcsModel<>() {
            private KSProblem problem;

            @Override
            public Problem<Integer> problem() {
                try {
                    problem = readInstance(instance);
                    return problem;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
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
            public int columnWidth() {
                return 25;
            }

        };

        Solve<Integer> solve = new Solve<>();

        SearchStatistics stats = solve.minimizeAcs(model);

        solve.onSolution(stats);




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
