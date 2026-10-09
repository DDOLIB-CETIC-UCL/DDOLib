package org.ddolib.examples.layered.gruler;

import java.io.IOException;
import org.ddolib.common.util.io.SolutionPrinter;
import org.ddolib.layered.modeling.AcsModel;
import org.ddolib.layered.modeling.FastLowerBound;
import org.ddolib.layered.modeling.Problem;
import org.ddolib.layered.modeling.Solvers;
import org.ddolib.layered.solver.Solution;

/**
 * Golomb Rule Problem (GRP) with Acs.
 *
 * <p>This program demonstrates how to:
 *
 * <ul>
 *   <li>Build a specific instance of the {@link GRProblem} (Golomb Ruler problem);
 *   <li>Wrap it into an {@link AcsModel} to define the optimization model used by the ACS
 *       algorithm;
 *   <li>Invoke the {@link Solvers} to perform the search via the {@code minimizeAcs()} method;
 *   <li>Monitor the search progress by printing incumbent (best found) solutions;
 *   <li>Display final search statistics at the end of execution.
 * </ul>
 */
public class GRAcsMain {

    private GRAcsMain() {}

    /**
     * Main entry point of the program.
     *
     * <p>Creates and solves a {@link GRProblem} instance using the ACS algorithm implemented by the
     * {@link Solvers} class.
     *
     * @param args command-line arguments (not used)
     * @throws IOException if any I/O error occurs during problem initialization or result export
     */
    public static void main(final String[] args) throws IOException {
        // Initialize the Golomb Ruler problem with n = 7 marks
        GRProblem problem = new GRProblem(7);
        // Define the ACS model for this problem
        final AcsModel<GRState> model =
                new AcsModel<>() {
                    @Override
                    public Problem<GRState> problem() {
                        return problem;
                    }

                    @Override
                    public FastLowerBound<GRState> lowerBound() {
                        return new GRFastLowerBound();
                    }

                    @Override
                    public int columnWidth() {
                        return 20;
                    }
                };

        Solution bestSolution =
                Solvers.minimizeAcs(
                        model,
                        (sol, s) -> {
                            SolutionPrinter.printSolution(s, sol);
                        });

        System.out.println(bestSolution.statistics());
        System.out.println(bestSolution);
    }
}
