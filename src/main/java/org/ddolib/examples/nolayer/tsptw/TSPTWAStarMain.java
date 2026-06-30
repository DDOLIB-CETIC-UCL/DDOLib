package org.ddolib.examples.nolayer.tsptw;

import org.ddolib.common.solver.layered.Solution;

import java.io.IOException;
import java.nio.file.Path;

public final class TSPTWAStarMain {
    public static void main(String[] args) throws IOException {
        final String instance = args.length == 0 ? Path.of("data", "TSPTW", "AFG", "rbg010a.tw").toString() : args[0];
        final TSPTWProblem problem = TSPTWProblem.fromFile(instance);
        final TSPTWModel model = new TSPTWModel(problem);

        org.ddolib.solving.astar.core.solver.nolayer.AStarSolver<TSPTWState> solver = new org.ddolib.solving.astar.core.solver.nolayer.AStarSolver<>(model);


        Solution bestSolution = solver.minimize(
                stats -> false,
                (sol, stats) -> {
                });

        System.out.println(bestSolution.statistics());
        System.out.println(bestSolution);
        System.out.println("Optimal TSPTW value: " + bestSolution.value());
    }
}
