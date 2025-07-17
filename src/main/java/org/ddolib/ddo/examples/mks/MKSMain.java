package org.ddolib.ddo.examples.mks;

import org.ddolib.ddo.core.*;
import org.ddolib.ddo.examples.knapsack.*;
import org.ddolib.ddo.heuristics.StateCoordinates;
import org.ddolib.ddo.heuristics.StateDistance;
import org.ddolib.ddo.heuristics.VariableHeuristic;
import org.ddolib.ddo.implem.dominance.SimpleDominanceChecker;
import org.ddolib.ddo.implem.frontier.SimpleFrontier;
import org.ddolib.ddo.implem.heuristics.DefaultVariableHeuristic;
import org.ddolib.ddo.implem.heuristics.FixedWidth;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

import static org.ddolib.ddo.implem.solver.Solvers.sequentialSolver;

public class MKSMain {

    public static void main(String[] args) throws IOException {
        final String instance = "data/MKS/MKP_1.txt";
        final MKSProblem problem = readInstance(instance);
        final MKSRelax relax = new MKSRelax();
        final MKSRanking ranking = new MKSRanking();
        final FixedWidth<MKSState> width = new FixedWidth<>(250);
        final VariableHeuristic<MKSState> varh = new DefaultVariableHeuristic<MKSState>();
        final SimpleDominanceChecker<MKSState, Integer> dominance = new SimpleDominanceChecker<>(new MKSDominance(),
                problem.nbVars());
        final Frontier<MKSState> frontier = new SimpleFrontier<>(ranking, CutSetType.LastExactLayer);
        final RelaxationStrat relaxStrat = RelaxationStrat.Cost;
        final StateDistance<MKSState> distance = new MKSDistance();
        final StateCoordinates<MKSState> coordinates = new MKSCoordinates();
        final int seed = 546645;

        final Solver solver = sequentialSolver(
                problem,
                relax,
                varh,
                ranking,
                width,
                frontier,
                dominance,
                relaxStrat,
                distance,
                coordinates,
                seed
        );


        long start = System.currentTimeMillis();
        SearchStatistics stats = solver.maximize(0, true);
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
        System.out.printf("Objective: %f%n", solver.bestValue().get());
        System.out.printf("Solution : %s%n", Arrays.toString(solution));
    }

    public static MKSProblem readInstance(final String fname) throws IOException {
        final File f = new File(fname);
        try (final BufferedReader bf = new BufferedReader(new FileReader(f))) {
            final PinReadContext context = new PinReadContext();
            bf.lines().forEachOrdered((String s) -> {
                if (context.isFirst) {
                    context.isFirst = false;
                    context.isSecond = true;
                    String[] tokens = s.split("\\s");
                    context.n = Integer.parseInt(tokens[0]);
                    context.dimensions = Integer.parseInt(tokens[1]);

                    if (tokens.length == 3) {
                        context.optimal = Integer.parseInt(tokens[2]);
                    }

                    context.profit = new int[context.n];
                    context.weights = new int[context.n][context.dimensions];
                    context.capa = new double[context.dimensions];
                } else if (context.isSecond) {
                    context.isSecond = false;
                    String[] tokens = s.split("\\s");
                    assert tokens.length == context.dimensions;
                    for (int i = 0; i < context.dimensions; i++) {
                        context.capa[i] = Integer.parseInt(tokens[i]);
                    }
                } else {
                    if (context.count < context.n) {
                        String[] tokens = s.split("\\s");
                        assert tokens.length == context.dimensions+1;
                        context.profit[context.count] = Integer.parseInt(tokens[0]);
                        for (int i = 0; i < context.dimensions; i++) {
                            context.weights[context.count][i] = Integer.parseInt(tokens[i+1]);
                        }
                        context.count++;
                    }
                }
            });
            System.out.println(Arrays.toString(context.capa));
            System.out.println(Arrays.toString(context.profit));
            System.out.println(Arrays.toString(context.weights));
            System.out.println(context.optimal);
            return new MKSProblem(context.capa, context.profit, context.weights, context.optimal);
        }
    }

    private static class PinReadContext {
        boolean isFirst = true;
        boolean isSecond = false;
        int n = 0;
        int dimensions = 0;
        int count = 0;
        double[] capa = new double[0];
        int[] profit = new int[0];
        int[][] weights = new int[0][0];
        Integer optimal = null;
    }

}
