package org.ddolib.nolayer.examples.misp;

import org.ddolib.nolayer.common.solver.Solution;
import org.ddolib.nolayer.modeling.AcsModel;
import org.ddolib.nolayer.modeling.FastLowerBound;
import org.ddolib.nolayer.modeling.NoLayerDominanceChecker;
import org.ddolib.nolayer.modeling.Problem;
import org.ddolib.util.io.SolutionPrinter;

import java.io.IOException;
import java.nio.file.Path;

public final class MispAcsMain {
    public static void main(String[] args) throws IOException {
        final String instance = args.length == 0 ? Path.of("data", "MISP", "tadpole_4_2.dot").toString() : args[0];
        final MispProblem problem = MispProblem.fromFile(instance);
        final MispModel baseModel = new MispModel(problem);

        final AcsModel<MispState> model = new AcsModel<>() {
            @Override
            public Problem<MispState> problem() {
                return problem;
            }

            @Override
            public FastLowerBound<MispState> lowerBound() {
                return baseModel.lowerBound();
            }

            @Override
            public NoLayerDominanceChecker<MispState> dominance() {
                return baseModel.dominance();
            }

            @Override
            public int columnWidth() {
                return 10;
            }
        };

        Solution bestSolution = org.ddolib.nolayer.modeling.Solvers.minimizeAcs(model, (sol, stats) -> {
            SolutionPrinter.printSolution(stats, sol);
        });

        System.out.println(bestSolution.statistics());
        System.out.println(bestSolution);
        System.out.println("Optimal MISP value: " + -bestSolution.value());
    }
}
