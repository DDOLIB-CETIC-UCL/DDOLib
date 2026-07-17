package org.ddolib.examples.nolayer.gruler;

import org.ddolib.nolayer.solver.Solution;
import org.ddolib.common.util.io.SolutionPrinter;

/**
 * Entry point for solving the Golomb Ruler Problem (GRP) using the nolayer A* search algorithm.
 */
public final class GRAStarMain {

    private GRAStarMain() {
    }

    /**
     * Entry point of the program. Builds a Golomb Ruler problem instance and solves it using the
     * A* search algorithm.
     *
     * @param args optional command-line argument giving the ruler order; if omitted, order 10 is used
     */
    public static void main(String[] args) {
        final int n = args.length == 0 ? 10 : Integer.parseInt(args[0]);
        final GRProblem problem = new GRProblem(n);
        final GRModel model = new GRModel(problem);

        org.ddolib.nolayer.solving.astar.core.solver.AStarSolver<GRState> solver = new org.ddolib.nolayer.solving.astar.core.solver.AStarSolver<>(
                model);

        Solution bestSolution = solver.minimize(
                stats -> false,
                (sol, stats) -> {
                    SolutionPrinter.printSolution(stats, sol);
                });

        System.out.println(bestSolution.statistics());
        System.out.println(bestSolution);
        System.out.println("Optimal GR value: " + bestSolution.statistics().incumbent());
    }
}
