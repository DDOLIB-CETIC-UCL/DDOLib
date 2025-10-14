package org.ddolib.examples.knapsack;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.Frontier;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.modeling.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class KSDdoMain {
    public static void main(final String[] args) throws IOException {

        final String instance = "data/Knapsack/instance_n1000_c1000_10_5_10_5_0";
        final DdoModel<Integer> model = new DdoModel<>() {

            private KSProblem problem = new KSProblem(instance);
            ;

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
            public Frontier<Integer> frontier() {
                return new SimpleFrontier<>(ranking(), CutSetType.Frontier);
            }

            @Override
            public boolean useCache() {
                return true;
            }

            @Override
            public WidthHeuristic<Integer> widthHeuristic() {
                return new FixedWidth<>(100);
            }

            @Override
            public VerbosityLevel verbosityLevel() {
                return VerbosityLevel.LARGE;
            }
        };

        Solvers<Integer> solver = new Solvers<>();

        SearchStatistics stats = solver.minimizeDdo(model, s -> s.runTimeMs() >= 10000, (sol, s) -> {
            System.out.println("--------------------");
            System.out.println("new incumbent found " + s.incumbent() + " at iteration " + s.nbIterations());
            System.out.println("New solution: " + sol + " at iteration " + s.nbIterations());
        });


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
