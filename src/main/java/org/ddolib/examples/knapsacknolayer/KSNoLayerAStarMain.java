package org.ddolib.examples.knapsacknolayer;

import org.ddolib.common.solver.Solution;
import org.ddolib.modeling.Solvers;
import org.ddolib.util.io.SolutionPrinter;

import java.io.IOException;
import java.nio.file.Path;

public final class KSNoLayerAStarMain {
    public static void main(String[] args) throws IOException {
        final String instance = args.length == 0 ? Path.of("data", "KP", "f10_l-d_kp_20_878").toString() : args[0];
        final KSNoLayerProblem problem = KSNoLayerProblem.fromFile(instance);
        final KSNoLayerModel model = new KSNoLayerModel(problem);

        org.ddolib.astar.core.solver.NoLayerAStarSolver<KSNoLayerState> solver = new org.ddolib.astar.core.solver.NoLayerAStarSolver<>(model);

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
