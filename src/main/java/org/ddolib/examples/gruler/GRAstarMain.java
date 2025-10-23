package org.ddolib.examples.gruler;

import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.modeling.Model;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solvers;
import org.ddolib.util.io.SolutionPrinter;

import java.io.IOException;

/**
 * Golomb Rule Problem (GRP) with AsTar.
 * Main class for solving the Golomb Ruler Problem (GRP) using the A* search algorithm.
 * <p>
 * This class demonstrates how to create a Golomb Ruler problem instance, define an A* search model,
 * and solve the problem using {@link Solvers#minimizeAstar(Model, java.util.function.BiConsumer)}.
 * The solution and search statistics are printed to the console.
 * </p>
 */
public class GRAstarMain {
    /**
     * Entry point of the application.
     * <p>
     * The method performs the following steps:
     * </p>
     * <ol>
     *     <li>Creates a Golomb Ruler problem instance with 8 marks.</li>
     *     <li>Defines an A* model for the problem.</li>
     *     <li>Solves the problem using the A* search algorithm.</li>
     *     <li>Prints the solution and search statistics to the console.</li>
     * </ol>
     * @param args command-line arguments (not used)
     * @throws IOException if an I/O error occurs while printing the solution
     */
    public static void main(final String[] args) throws IOException {
        GRProblem problem = new GRProblem(8);
        final Model<GRState> model = new Model<>() {
            @Override
            public Problem<GRState> problem() {
                return problem;
            }

        };

        SearchStatistics stats = Solvers.minimizeAstar(model, (sol, s) -> {
            SolutionPrinter.printSolution(s,sol);
        });

        System.out.println(stats);
    }
}
