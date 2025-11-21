package org.ddolib.examples.pdp;

import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.modeling.Model;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solvers;
import org.ddolib.util.io.SolutionPrinter;

import java.io.IOException;
import java.util.Random;

import static org.ddolib.examples.pdp.PDPGenerator.genInstance;
/**
 * Single Vehicle Pick-up and Delivery Problem (PDP) with AsTar.
 * Main class for solving the <b>Pickup and Delivery Problem (PDP)</b> using the
 * <b>A* (A-star) search algorithm</b>.
 * <p>
 * This class demonstrates how to configure and execute an A* solver
 * on a randomly generated PDP instance.
 * The PDP consists of a set of paired pickup and delivery requests that must be
 * scheduled optimally, typically to minimize total travel cost or time while respecting
 * precedence and capacity constraints.
 * </p>
 *
 * <p><b>Execution details:</b></p>
 * <ul>
 *   <li>A random PDP instance is generated using {@link PDPGenerator#genInstance(int, int, int, java.util.Random)}.</li>
 *   <li>The problem is wrapped into a {@link Model} that specifies:
 *     <ul>
 *       <li>the {@link Problem} to solve ({@link PDPProblem}),</li>
 *       <li>a fast lower bound through {@link PDPFastLowerBound} to guide A* search.</li>
 *     </ul>
 *   </li>
 *   <li>The solver is then launched using {@link Solvers#minimizeAstar(Model, java.util.function.BiConsumer)}.</li>
 *   <li>Each discovered solution is printed using {@link SolutionPrinter#printSolution(SearchStatistics, int[])}.</li>
 *   <li>Search statistics are displayed at the end of the execution.</li>
 * </ul>
 *
 * <p><b>Usage example:</b></p>
 * <pre>{@code
 * // Run the A* PDP solver
 * java PDPAstarMain
 *
 * // Example output:
 * Solution: [0, 2, 5, ...]
 * SearchStatistics{status=OPTIMAL, iterations=..., time=...}
 * }</pre>
 *
 * <p><b>Notes:</b></p>
 * <ul>
 *   <li>The PDP instance is generated with a fixed random seed ({@code new Random(1)})
 *       to ensure reproducible experiments.</li>
 *   <li>This class serves as a demonstration of how to apply A* to combinatorial optimization
 *       within the PDP framework.</li>
 * </ul>
 *
 * @see PDPProblem
 * @see PDPGenerator
 * @see PDPFastLowerBound
 * @see PDPState
 * @see Solvers#minimizeAstar(Model, java.util.function.BiConsumer)
 */
public final class PDPAstarMain {

    /**
     * Entry point for solving a randomly generated Pickup and Delivery Problem (PDP)
     * instance using the A* algorithm.
     * <p>
     * The instance is created with fixed parameters and solved by the A* search framework
     * provided by the {@link Solvers} utility.
     * </p>
     *
     * @param args optional command-line arguments (not used in this example)
     * @throws IOException if an error occurs while reading or generating the instance
     */
    public static void main(final String[] args) throws IOException {
        final PDPProblem problem = genInstance(18, 2, 3, new Random(1));
        Model<PDPState> model = new Model<>() {
            @Override
            public Problem<PDPState> problem() {
                return problem;
            }

            @Override
            public PDPFastLowerBound lowerBound() {
                return new PDPFastLowerBound(problem);
            }
        };

        SearchStatistics stats = Solvers.minimizeAstar(model, (sol, s) -> {
            SolutionPrinter.printSolution(s,sol);
        });
        System.out.println(stats);
    }

}
