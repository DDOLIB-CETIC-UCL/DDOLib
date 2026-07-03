package org.ddolib.examples.nolayer.misp;

import org.ddolib.nolayer.modeling.*;
import org.ddolib.nolayer.solver.Solution;

import java.io.IOException;
import java.nio.file.Path;

public class MispAwAstarMain {
    public static void main(String[] args) throws IOException {
        final String instance = args.length == 0 ? Path.of("data", "MISP", "weighted.dot").toString() : args[0];
        final MispProblem problem = MispProblem.fromFile(instance);
        final MispModel baseModel = new MispModel(problem);

        final AwAstarModel<MispState> model = new AwAstarModel<MispState>() {
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

        };

        Solution bestSolution = Solvers.minimizeAwAstar(model, (sol, stats) -> {
            // SolutionPrinter.printSolution(stats, sol);
        });

        System.out.println(bestSolution.statistics());
        System.out.println(bestSolution);
        System.out.println("Optimal MISP value: " + -bestSolution.value());
    }
}
