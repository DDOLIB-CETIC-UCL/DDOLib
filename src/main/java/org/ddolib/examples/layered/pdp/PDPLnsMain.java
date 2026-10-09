package org.ddolib.examples.layered.pdp;

import static org.ddolib.examples.layered.pdp.PDPGenerator.genInstance;

import java.io.IOException;
import java.util.Random;
import org.ddolib.common.heuristics.width.FixedWidth;
import org.ddolib.common.heuristics.width.WidthHeuristic;
import org.ddolib.common.util.io.SolutionPrinter;
import org.ddolib.layered.modeling.LnsModel;
import org.ddolib.layered.modeling.Problem;
import org.ddolib.layered.modeling.Solvers;
import org.ddolib.layered.solver.Solution;

/**
 * Entry point for solving the Single Vehicle Pick-up and Delivery Problem (PDP) using a Large
 * Neighborhood Search (LNS) approach combined with Decision Diagram Optimization (DDO).
 *
 * <p>The Single Vehicle Pick-up and Delivery Problem consists in determining a sequence of pick-ups
 * and deliveries for a single vehicle such that:
 *
 * <ul>
 *   <li>All requests (pick-up and corresponding delivery) are served
 *   <li>Vehicle capacity and precedence constraints are respected
 *   <li>The total cost or travel distance is minimized
 * </ul>
 *
 * <p>This class demonstrates how to:
 *
 * <ul>
 *   <li>Generate a PDP instance programmatically
 *   <li>Define a {@code LnsModel} with problem-specific components
 *   <li>Use a lower bound heuristic to guide the search
 *   <li>Use a ranking heuristic to prioritize states
 *   <li>Control the decision diagram width
 *   <li>Run a Large Neighborhood Search (LNS) optimization
 *   <li>Print intermediate and final solutions
 * </ul>
 *
 * <h2>Instance Configuration</h2>
 *
 * <p>In this example, a random PDP instance is generated with:
 *
 * <ul>
 *   <li>18 requests
 *   <li>2 vehicles (single vehicle may still be assumed for sequencing)
 *   <li>3 maximum capacity per vehicle
 *   <li>Random seed: 1
 * </ul>
 *
 * <h2>Model Components</h2>
 *
 * <ul>
 *   <li>{@link PDPProblem} – defines the pick-up and delivery requests
 *   <li>{@link PDPFastLowerBound} – provides a fast lower bound on the total cost
 *   <li>{@link PDPRanking} – ranks states during decision diagram compilation
 *   <li>{@link FixedWidth} – limits the decision diagram width
 * </ul>
 *
 * <h2>Search Configuration</h2>
 *
 * <ul>
 *   <li>Search strategy: Large Neighborhood Search (LNS)
 *   <li>Time limit: 10,000 milliseconds (10 seconds)
 *   <li>Width heuristic: fixed width of 1,000 nodes per layer
 * </ul>
 *
 * <h2>Output</h2>
 *
 * <p>The program prints:
 *
 * <ul>
 *   <li>Intermediate solutions during the search
 *   <li>Final solution statistics
 *   <li>The best sequence of pick-ups and deliveries found
 * </ul>
 *
 * @see PDPProblem
 * @see PDPState
 * @see PDPFastLowerBound
 * @see PDPRanking
 * @see LnsModel
 * @see Solvers#minimizeLns
 * @see Solution
 */
public class PDPLnsMain {

    private PDPLnsMain() {}

    /**
     * Main entry point of the program.
     *
     * <p>Generates a PDP instance, configures the LNS model with problem-specific heuristics, and
     * runs the optimization process.
     *
     * @param args command-line arguments (currently unused)
     * @throws IOException if any file access occurs (not used here but for consistency)
     */
    public static void main(final String[] args) throws IOException {

        final PDPProblem problem = genInstance(18, 2, 3, new Random(1));

        LnsModel<PDPState> model =
                new LnsModel<>() {
                    @Override
                    public Problem<PDPState> problem() {
                        return problem;
                    }

                    @Override
                    public PDPFastLowerBound lowerBound() {
                        return new PDPFastLowerBound(problem);
                    }

                    @Override
                    public PDPRanking ranking() {
                        return new PDPRanking();
                    }

                    @Override
                    public WidthHeuristic<PDPState> widthHeuristic() {
                        return new FixedWidth<>(1000);
                    }
                };

        Solution bestSolution =
                Solvers.minimizeLns(
                        model,
                        s -> s.runtime() > 10000,
                        (sol, s) -> {
                            SolutionPrinter.printSolution(s, sol);
                        });

        System.out.println(bestSolution.statistics());
        System.out.println(bestSolution);
    }
}
