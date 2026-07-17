package org.ddolib.examples.nolayer.tsptw;

import org.ddolib.nolayer.modeling.AwAstarModel;
import org.ddolib.nolayer.modeling.FastLowerBound;
import org.ddolib.nolayer.modeling.NoLayerDominanceChecker;
import org.ddolib.nolayer.modeling.Problem;
import org.ddolib.nolayer.solver.Solution;
import org.ddolib.nolayer.solving.awastar.core.solver.AwAstarSolver;
import org.ddolib.common.util.io.SolutionPrinter;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Main class to solve a Traveling Salesman Problem with Time Windows (TSPTW) instance
 * using the no-layer Anytime Weighted A* (AWA*) algorithm.
 */
public class TspTwAwAstarMain {

    private TspTwAwAstarMain() {
    }

    /**
     * Entry point of the program. Builds a TSPTW instance and solves it using the AWA* algorithm.
     *
     * @param args optional command-line argument: path to the TSPTW instance file
     *             (default: {@code data/TSPTW/AFG/rbg172a.tw})
     * @throws IOException if there is an error reading the instance file
     */
    public static void main(String[] args) throws IOException {
        final String instance = args.length == 0 ? Path.of("data", "TSPTW", "AFG", "rbg172a.tw").toString() : args[0];
        final TSPTWProblem problem = TSPTWProblem.fromFile(instance);

        var model = new AwAstarModel<TSPTWState>() {
            @Override
            public Problem<TSPTWState> problem() {
                return problem;
            }

            @Override
            public double weight() {
                return 7.5;
            }

            @Override
            public FastLowerBound<TSPTWState> lowerBound() {
                return new TSPTWFlb(problem);
            }

            @Override
            public NoLayerDominanceChecker<TSPTWState> dominance() {
                return new TSPTWDominance();
            }
        };
        var solver = new AwAstarSolver<>(model);

        Solution bestSolution = solver.minimize(searchStatistics -> searchStatistics.runtime() > 1000, (sol, stat) -> {
            SolutionPrinter.printSolution(stat, sol);
        });

        System.out.println(bestSolution.statistics());
        System.out.println(bestSolution);


    }
}
