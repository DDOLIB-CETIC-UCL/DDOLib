package org.ddolib.examples.grulernolayer;

import org.ddolib.common.solver.Solution;
import org.ddolib.modeling.Solvers;
import org.ddolib.util.io.SolutionPrinter;

public final class GRNoLayerAStarMain {
    public static void main(String[] args) {
        final int n = args.length == 0 ? 10 : Integer.parseInt(args[0]);
        final GRNoLayerProblem problem = new GRNoLayerProblem(n);
        final GRNoLayerModel model = new GRNoLayerModel(problem);

        org.ddolib.astar.core.solver.NoLayerAStarSolver<GRNoLayerState> solver = new org.ddolib.astar.core.solver.NoLayerAStarSolver<>(
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
