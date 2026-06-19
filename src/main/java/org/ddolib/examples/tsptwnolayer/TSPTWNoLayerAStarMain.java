package org.ddolib.examples.tsptwnolayer;

import org.ddolib.common.solver.Solution;
import org.ddolib.modeling.Solvers;
import org.ddolib.util.io.SolutionPrinter;

import java.io.IOException;
import java.nio.file.Path;

public final class TSPTWNoLayerAStarMain {
    public static void main(String[] args) throws IOException {
        final String instance = args.length == 0 ? Path.of("data", "TSPTW", "AFG", "rbg010a.tw").toString() : args[0];
        final TSPTWNoLayerProblem problem = TSPTWNoLayerProblem.fromFile(instance);
        final TSPTWNoLayerModel model = new TSPTWNoLayerModel(problem);

        org.ddolib.astar.core.solver.NoLayerAStarSolver<TSPTWNoLayerState> solver = new org.ddolib.astar.core.solver.NoLayerAStarSolver<>(model);

        Solution bestSolution = solver.minimize(
                stats -> false,
                (sol, stats) -> {
                    SolutionPrinter.printSolution(stats, sol);
                }
        );

        System.out.println(bestSolution.statistics());
        System.out.println(bestSolution);
        System.out.println("Optimal TSPTW value: " + bestSolution.value());
    }
}
