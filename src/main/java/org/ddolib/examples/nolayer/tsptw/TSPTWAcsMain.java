package org.ddolib.examples.nolayer.tsptw;

import org.ddolib.nolayer.modeling.AcsModel;
import org.ddolib.nolayer.modeling.FastLowerBound;
import org.ddolib.nolayer.modeling.NoLayerDominanceChecker;
import org.ddolib.nolayer.modeling.Problem;
import org.ddolib.nolayer.solver.Solution;
import org.ddolib.common.util.io.SolutionPrinter;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Main class to solve a Traveling Salesman Problem with Time Windows (TSPTW) instance
 * using the no-layer ACS (Anytime Column Search) algorithm.
 */
public final class TSPTWAcsMain {

    private TSPTWAcsMain() {
    }

    /**
     * Entry point of the program. Builds a TSPTW instance and solves it using the ACS algorithm.
     *
     * @param args optional command-line argument: path to the TSPTW instance file
     *             (default: {@code data/TSPTW/AFG/rbg010a.tw})
     * @throws IOException if there is an error reading the instance file
     */
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

        Solution bestSolution = org.ddolib.nolayer.modeling.Solvers.minimizeAcs(model, (sol, stats) -> {
            SolutionPrinter.printSolution(stats, sol);
        });

        System.out.println(bestSolution.statistics());
        System.out.println(bestSolution);
        System.out.println("Optimal TSPTW value: " + bestSolution.value());
    }
}
