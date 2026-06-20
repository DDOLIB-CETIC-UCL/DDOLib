package org.ddolib.examples.nolayer.knapsack;

import org.ddolib.common.dominance.NoLayerDominanceChecker;
import org.ddolib.common.solver.Solution;
import org.ddolib.modeling.nolayer.Solvers;
import org.ddolib.modeling.nolayer.AcsModel;
import org.ddolib.modeling.nolayer.FastLowerBound;
import org.ddolib.modeling.nolayer.Problem;
import org.ddolib.util.io.SolutionPrinter;

import java.io.IOException;
import java.nio.file.Path;

public final class KSAcsMain {
    public static void main(String[] args) throws IOException {
        final String instance = args.length == 0 ? Path.of("data", "Knapsack",
                "instance_n1000_c1000_10_5_10_5_0").toString() : args[0];
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

            @Override
            public NoLayerDominanceChecker<KSState> dominance() {
                return baseModel.dominance();
            }

            @Override
            public int columnWidth() {
                return 10;
            }
        };

        Solution bestSolution = org.ddolib.modeling.nolayer.Solvers.minimizeAcs(model, (sol, stats) -> {
            SolutionPrinter.printSolution(stats, sol);
        });

        System.out.println(bestSolution.statistics());
        System.out.println(bestSolution);
        System.out.println("Optimal KS value: " + -bestSolution.value());
        try {
            int[] solArray = bestSolution.solution();
            double val = problem.evaluate(solArray);
            System.out.println("Evaluated KS value: " + val);
        } catch (Exception e) {
            System.out.println("ACS EVALUATION ERROR: " + e.getMessage());
        }
    }
}
