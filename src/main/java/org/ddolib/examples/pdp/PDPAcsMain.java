package org.ddolib.examples.pdp;

import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.modeling.AcsModel;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solvers;
import org.ddolib.util.io.SolutionPrinter;

import java.io.IOException;
import java.util.Random;
/**
 * Single Vehicle Pick-up and Delivery Problem (PDP) with Acs.
 * Main class for solving the <b>Pickup and Delivery Problem (PDP)</b> using the
 * <b>Anytime Column Search (ACS)</b> algorithm.
 * <p>
 * This class demonstrates how to configure and run the ACS-based solver
 * on an automatically generated PDP instance.
 * The PDP consists of a set of pickup and delivery requests that must be
 * scheduled while respecting precedence constraints and minimizing the total travel cost or time.
 * </p>
 *
 * <p><b>Execution details:</b></p>
 * <ul>
 *   <li>A random PDP instance is generated using {@link PDPGenerator#genInstance(int, int, int, Random)}.</li>
 *   <li>The problem is modeled through an {@link AcsModel}, which defines:
 *     <ul>
 *       <li>the {@link Problem} to solve ({@link PDPProblem}),</li>
 *       <li>a fast lower bound via {@link PDPFastLowerBound},</li>
 *       <li>and the search column width (here set to 30).</li>
 *     </ul>
 *   </li>
 *   <li>The solver is then executed using {@link Solvers#minimizeAcs(AcsModel, java.util.function.BiConsumer)}.</li>
 *   <li>Results and statistics are printed to the standard output.</li>
 * </ul>
 *
 * <p><b>Usage example:</b></p>
 * <pre>{@code
 * // Run from the command line (no arguments required)
 * java PDPAcsMain
 *
 * // Sample output:
 * RemainingJobs [...]
 * ----> currentTime 42
 * SearchStatistics{status=OPTIMAL, iterations=..., time=...}
 * }</pre>
 *
 * <p><b>Notes:</b></p>
 * <ul>
 *   <li>The instance generation is controlled by a fixed random seed ({@code new Random(1)})
 *       for reproducibility.</li>
 *   <li>This example is primarily meant for experimentation and demonstration of the ACS solver.</li>
 * </ul>
 *
 * @see PDPProblem
 * @see PDPGenerator
 * @see PDPFastLowerBound
 * @see PDPState
 * @see AcsModel
 * @see Solvers#minimizeAcs(AcsModel, java.util.function.BiConsumer)
 */
public final class PDPAcsMain {

    /**
     * Entry point for the PDP ACS solver.
     * <p>
     * Generates a random Pickup and Delivery Problem instance and solves it
     * using the Adaptive Column Search (ACS) framework.
     * </p>
     *
     * @param args optional command-line arguments (not used in this version)
     * @throws IOException if an error occurs during instance generation or file access
     */
    public static void main(final String[] args) throws IOException {

        final PDPProblem problem = PDPGenerator.genInstance(18, 2, 3, new Random(1));
        AcsModel<PDPState> model = new AcsModel<>() {

            @Override
            public Problem<PDPState> problem() {
                return problem;
            }

            @Override
            public PDPFastLowerBound lowerBound() {
                return new PDPFastLowerBound(problem);
            }

            @Override
            public int columnWidth() {
                return 30;
            }
        };

        SearchStatistics stats = Solvers.minimizeAcs(model, (sol, s) -> {
            SolutionPrinter.printSolution(s,sol);
        });
        System.out.println(stats);
    }

}
