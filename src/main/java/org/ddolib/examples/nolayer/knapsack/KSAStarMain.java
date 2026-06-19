package org.ddolib.examples.nolayer.knapsack;

import org.ddolib.common.solver.Solution;
import org.ddolib.modeling.nolayer.Solvers;
import org.ddolib.util.io.SolutionPrinter;

import java.io.IOException;
import java.nio.file.Path;

public final class KSAStarMain {
    public static void main(String[] args) throws IOException {
        final String instance = args.length == 0 ? Path.of("data", "KP", "f10_l-d_kp_20_878").toString() : args[0];
        final KSProblem problem = KSProblem.fromFile(instance);
        final KSModel model = new KSModel(problem);

        org.ddolib.solving.astar.core.solver.nolayer.AStarSolver<KSState> solver = new org.ddolib.solving.astar.core.solver.nolayer.AStarSolver<>(model);

        Solution bestSolution = solver.minimize(
                stats -> false,
                (sol, stats) -> {
                    SolutionPrinter.printSolution(stats, sol);
                }
        );

        System.out.println(bestSolution.statistics());
        System.out.println(bestSolution);
        System.out.println("Optimal KS value: " + -bestSolution.value());
    }
}
