package org.ddolib.examples.nolayer.misp;

import org.ddolib.nolayer.modeling.Solvers;
import org.ddolib.nolayer.solver.Solution;
import org.ddolib.common.util.io.SolutionPrinter;

import java.io.IOException;
import java.nio.file.Path;

/**
 * The Maximum Independent Set Problem (MISP) with AStar.
 * Entry point for solving the Maximum Independent Set Problem (MISP) using an A* solver, in
 * the no-layer modeling API.
 */
public final class MispAStarMain {

    private MispAStarMain() {
    }

    /**
     * Main method to execute the A* solver on a given MISP instance.
     * <p>
     * If no command-line argument is provided, a default instance
     * <code>data/MISP/tadpole_4_2.dot</code> is used.
     *
     * @param args optional command-line arguments; args[0] can be the path to the MISP instance file
     * @throws IOException if there is an error reading the problem instance from the file
     */
    public static void main(String[] args) throws IOException {
        final String instance = args.length == 0 ? Path.of("data", "MISP", "tadpole_4_2.dot").toString() : args[0];
        final MispProblem problem = MispProblem.fromFile(instance);
        final MispModel model = new MispModel(problem);


        Solution bestSolution = Solvers.minimizeAstar(model,
                (sol, stats) -> {
                    SolutionPrinter.printSolution(stats, sol);
                }
        );

        System.out.println(bestSolution.statistics());
        System.out.println(bestSolution);
        System.out.println("Optimal MISP value: " + -bestSolution.statistics().incumbent());
    }
}
