package org.ddolib.examples.gruler;

import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.modeling.AcsModel;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solvers;
import org.ddolib.util.io.SolutionPrinter;

import java.io.IOException;

/**
 * ########## Golomb Rule Problem (GRP) ################

 * <p>
 * This program demonstrates how to:
 * </p>
 * <ul>
 *   <li>Build a specific instance of the {@link GRProblem} (Golomb Ruler problem);</li>
 *   <li>Wrap it into an {@link AcsModel} to define the optimization model used by the ACS algorithm;</li>
 *   <li>Invoke the {@link Solvers} to perform the search via the {@code minimizeAcs()} method;</li>
 *   <li>Monitor the search progress by printing incumbent (best found) solutions;</li>
 *   <li>Display final search statistics at the end of execution.</li>
 * </ul>
 */
public class GRAcsMain {
    /**
     * Main entry point of the program.
     * <p>
     * Creates and solves a {@link GRProblem} instance using the ACS
     * algorithm implemented by the {@link Solvers} class.
     * </p>
     *
     * @param args command-line arguments (not used)
     * @throws IOException if any I/O error occurs during problem initialization or result export
     */

    public static void main(final String[] args) throws IOException {
        // Initialize the Golomb Ruler problem with n = 7 marks
        GRProblem problem = new GRProblem(7);
        // Define the ACS model for this problem
        final AcsModel<GRState> model = new AcsModel<>() {
            @Override
            public Problem<GRState> problem() {
                return problem;
            }

            @Override
            public int columnWidth() {
                return 20;
            }
        };

        SearchStatistics stats = Solvers.minimizeAcs(model, (sol, s) -> {
            SolutionPrinter.printSolution(s,sol);
        });

        System.out.println(stats);
    }
}
