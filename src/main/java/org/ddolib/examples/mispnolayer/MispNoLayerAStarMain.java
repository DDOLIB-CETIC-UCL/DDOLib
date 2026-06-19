package org.ddolib.examples.mispnolayer;

import org.ddolib.common.solver.Solution;
import org.ddolib.modeling.Solvers;
import org.ddolib.util.io.SolutionPrinter;

import java.io.IOException;
import java.nio.file.Path;

public final class MispNoLayerAStarMain {
    public static void main(String[] args) throws IOException {
        final String instance = args.length == 0 ? Path.of("data", "MISP", "tadpole_4_2.dot").toString() : args[0];
        final MispNoLayerProblem problem = MispNoLayerProblem.fromFile(instance);
        final MispNoLayerModel model = new MispNoLayerModel(problem);

        org.ddolib.astar.core.solver.NoLayerAStarSolver<MispNoLayerState> solver = new org.ddolib.astar.core.solver.NoLayerAStarSolver<>(model);

        Solution bestSolution = solver.minimize(
                stats -> false,
                (sol, stats) -> {
                    SolutionPrinter.printSolution(stats, sol);
                }
        );

        System.out.println(bestSolution.statistics());
        System.out.println(bestSolution);
        System.out.println("Optimal MISP value: " + -bestSolution.value());
    }
}
