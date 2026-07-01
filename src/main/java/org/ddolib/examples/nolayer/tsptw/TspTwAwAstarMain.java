package org.ddolib.examples.nolayer.tsptw;

import org.ddolib.common.dominance.NoLayerDominanceChecker;
import org.ddolib.common.solver.nolayer.Solution;
import org.ddolib.modeling.nolayer.AwAstarModel;
import org.ddolib.modeling.nolayer.FastLowerBound;
import org.ddolib.modeling.nolayer.Problem;
import org.ddolib.solving.awastar.core.solver.nolayer.AwAstarSolver;
import org.ddolib.util.io.SolutionPrinter;

import java.io.IOException;
import java.nio.file.Path;

public class TspTwAwAstarMain {

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
                return new TSPTWFbl(problem);
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
