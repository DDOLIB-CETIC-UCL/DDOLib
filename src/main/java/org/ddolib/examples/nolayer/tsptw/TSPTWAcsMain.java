package org.ddolib.examples.nolayer.tsptw;

import org.ddolib.common.dominance.NoLayerDominanceChecker;
import org.ddolib.common.solver.layered.Solution;
import org.ddolib.modeling.nolayer.AcsModel;
import org.ddolib.modeling.nolayer.FastLowerBound;
import org.ddolib.modeling.nolayer.Problem;
import org.ddolib.util.io.SolutionPrinter;

import java.io.IOException;
import java.nio.file.Path;

public final class TSPTWAcsMain {
    public static void main(String[] args) throws IOException {
        final String instance = args.length == 0 ? Path.of("data", "TSPTW", "AFG", "rbg010a.tw").toString() : args[0];
        final TSPTWProblem problem = TSPTWProblem.fromFile(instance);
        final TSPTWModel baseModel = new TSPTWModel(problem);

        final AcsModel<TSPTWState> model = new AcsModel<>() {
            @Override
            public Problem<TSPTWState> problem() {
                return problem;
            }

            @Override
            public FastLowerBound<TSPTWState> lowerBound() {
                return baseModel.lowerBound();
            }

            @Override
            public NoLayerDominanceChecker<TSPTWState> dominance() {
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
        System.out.println("Optimal TSPTW value: " + bestSolution.value());
    }
}
