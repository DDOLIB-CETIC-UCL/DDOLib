package org.ddolib.examples.nolayer.tsptw;

import org.ddolib.nolayer.solver.Solution;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Main class to solve a Traveling Salesman Problem with Time Windows (TSPTW) instance
 * using the no-layer A* algorithm.
 */
public final class TSPTWAStarMain {

    private TSPTWAStarMain() {
    }

    /**
     * Entry point of the program. Builds a TSPTW instance and solves it using the A* algorithm.
     *
     * @param args optional command-line argument: path to the TSPTW instance file
     *             (default: {@code data/TSPTW/AFG/rbg010a.tw})
     * @throws IOException if there is an error reading the instance file
     */
    public static void main(String[] args) throws IOException {
        final String instance = args.length == 0 ? Path.of("data", "TSPTW", "AFG", "rbg010a.tw").toString() : args[0];
        final TSPTWProblem problem = TSPTWProblem.fromFile(instance);
        final TSPTWModel model = new TSPTWModel(problem);

        org.ddolib.nolayer.solving.astar.core.solver.AStarSolver<TSPTWState> solver = new org.ddolib.nolayer.solving.astar.core.solver.AStarSolver<>(model);


        Solution bestSolution = solver.minimize(
                stats -> false,
                (sol, stats) -> {
                });

        System.out.println(bestSolution.statistics());
        System.out.println(bestSolution);
        System.out.println("Optimal TSPTW value: " + bestSolution.statistics().incumbent());
    }
}
