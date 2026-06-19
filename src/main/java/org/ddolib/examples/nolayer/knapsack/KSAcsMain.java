package org.ddolib.examples.nolayer.knapsack;

import org.ddolib.common.dominance.NoLayerDominanceChecker;
import org.ddolib.common.solver.Solution;
import org.ddolib.modeling.Solvers;
import org.ddolib.modeling.nolayer.NoLayerAcsModel;
import org.ddolib.modeling.nolayer.NoLayerFastLowerBound;
import org.ddolib.modeling.nolayer.NoLayerProblem;
import org.ddolib.util.io.SolutionPrinter;

import java.io.IOException;
import java.nio.file.Path;

public final class KSAcsMain {
    public static void main(String[] args) throws IOException {
        final String instance = args.length == 0 ? Path.of("data", "KP", "f10_l-d_kp_20_878").toString() : args[0];
        final KSProblem problem = KSProblem.fromFile(instance);
        final KSModel baseModel = new KSModel(problem);

        final NoLayerAcsModel<KSState> model = new NoLayerAcsModel<>() {
            @Override
            public NoLayerProblem<KSState> problem() {
                return problem;
            }

            @Override
            public NoLayerFastLowerBound<KSState> lowerBound() {
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

        Solution bestSolution = Solvers.minimizeNoLayerAcs(model, (sol, stats) -> {
            SolutionPrinter.printSolution(stats, sol);
        });

        System.out.println(bestSolution.statistics());
        System.out.println(bestSolution);
        System.out.println("Optimal KS value: " + -bestSolution.value());
    }
}
