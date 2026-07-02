package org.ddolib.nolayer.examples.gruler;

import org.ddolib.layered.common.solver.Solution;
import org.ddolib.util.io.SolutionPrinter;

public final class GRAStarMain {
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
        System.out.println("Optimal GR value: " + bestSolution.value());
    }
}
