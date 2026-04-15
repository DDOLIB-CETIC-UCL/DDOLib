package org.ddolib.examples.pdptw;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.common.solver.Solution;
import org.ddolib.examples.pdp.PDPFastLowerBound;
import org.ddolib.examples.pdp.PDPGenerator;
import org.ddolib.examples.pdp.PDPProblem;
import org.ddolib.examples.pdp.PDPState;
import org.ddolib.modeling.AcsModel;
import org.ddolib.modeling.Model;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solvers;
import org.ddolib.util.io.SolutionPrinter;

import java.io.IOException;
import java.util.Random;

/**
 * Single Vehicle Pick-up and Delivery Problem with Time Window (PDPTW) with Ddo.
 * Main class for solving the <b>Pickup and Delivery Problem with Time Window (PDPTW)</b> using the
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
 *   <li>A random PDPTW instance is generated using
 *       {@link PDPTWGenerator#genInstance(int, int, int, java.util.Random)}.</li>
 *   <li>The problem is wrapped into a {@link Model} that specifies:
 *     <ul>
 *       <li>the {@link PDPTWProblem} definition,</li>
 *       <li>a fast lower bound through {@link PDPTWFastLowerBound} to guide ACS.</li>
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
 * java PDPTWAcsMain
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
 * @see PDPTWProblem
 * @see PDPTWGenerator
 * @see PDPTWFastLowerBound
 * @see PDPTWState
 * @see AcsModel
 * @see Solvers#minimizeAcs(AcsModel, java.util.function.BiConsumer)
 */
public final class PDPTWAcsMain {

    /**
     * Entry point for the PDP ACS solver.
     * <p>
     * Generates a random Pickup and Delivery Problem instance with Time Window and solves it
     * using the Adaptive Column Search (ACS) framework.
     * </p>
     *
     * @param args optional command-line arguments (not used in this version)
     * @throws IOException if an error occurs during instance generation or file access
     */
    public static void main(final String[] args) throws IOException {

        final PDPTWProblem problem = PDPTWGenerator.genInstance(30, 3, 5, new Random(2));
        AcsModel<PDPTWState> model = new AcsModel<>() {

            public Problem<PDPTWState> problem() {
                return problem;
            }

            @Override
            public PDPTWFastLowerBound lowerBound() {
                return new PDPTWFastLowerBound(problem);
            }

            @Override
            public int columnWidth() {
                return 100;
            }

            @Override
            public DominanceChecker<PDPTWState> dominance() {
                return new SimpleDominanceChecker<PDPTWState>(new PDPTWDominance(), problem.nbVars());
            }
        };

        Solution bestSolution = Solvers.minimizeAcs(model, (sol, s) -> {
            SolutionPrinter.printSolution(s, sol);
        });

        System.out.println(bestSolution.statistics());
        System.out.println(new PDPTWSolution(problem, bestSolution, -1));
    }

}
