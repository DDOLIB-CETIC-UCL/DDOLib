package org.ddolib.examples.pdptw;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.common.solver.Solution;
import org.ddolib.examples.pdp.PDPFastLowerBound;
import org.ddolib.examples.pdp.PDPGenerator;
import org.ddolib.examples.pdp.PDPProblem;
import org.ddolib.examples.pdp.PDPState;
import org.ddolib.modeling.Model;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solvers;
import org.ddolib.util.io.SolutionPrinter;

import java.io.IOException;
import java.util.Random;

import static org.ddolib.examples.pdp.PDPGenerator.genInstance;

/**
 * Single Vehicle Pick-up and Delivery Problem with Time Window (PDPTW) with Ddo.
 * Main class for solving the <b>Pickup and Delivery Problem with Time Window (PDPTW)</b> using the
 * <b>A* (A-star) search algorithm</b>.
 * <p>
 * This class demonstrates how to configure and execute an A* solver
 * for a randomly generated PDPTW instance. The PDPTW involves scheduling a set
 * of pickup and delivery tasks while minimizing the overall cost or time,
 * typically under precedence, capacity and time window constraints.
 * </p>
 *
 * <p><b>Execution details:</b></p>
 * <ul>
 *   <li>A random PDPTW instance is generated using
 *       {@link PDPTWGenerator#genInstance(int, int, int, java.util.Random)}.</li>
 *   <li>The problem is wrapped into a {@link Model} that specifies:
 *     <ul>
 *       <li>the {@link PDPTWProblem} definition,</li>
 *       <li>a fast lower bound through {@link PDPTWFastLowerBound} to guide A* search.</li>
 *     </ul>
 *   </li>
 *   <li>The solver is then launched using {@link Solvers#minimizeAstar(Model, java.util.function.BiConsumer)}.</li>
 *   <li>Each discovered solution is printed using {@link SolutionPrinter#printSolution(SearchStatistics, int[])}.</li>
 *   <li>Search statistics are displayed at the end of the execution.</li>
 * </ul>
 *
 * <p><b>Usage example:</b></p>
 * <pre>{@code
 * // Run the A* PDPTW solver
 * java PDPTWAstarMain
 *
 * // Example output:
 * Solution: [0, 2, 5, ...]
 * SearchStatistics{status=OPTIMAL, iterations=..., time=...}
 * }</pre>
 *
 * <p><b>Notes:</b></p>
 * <ul>
 *   <li>The PDPTW instance is generated with a fixed random seed ({@code new Random(1)})
 *       to ensure reproducible results.</li>
 *   <li>This class serves as a demonstration of how to apply A* to combinatorial optimization
 *       within the PDPTW framework.</li>
 * </ul>
 *
 * @see PDPTWProblem
 * @see PDPTWGenerator
 * @see PDPTWFastLowerBound
 * @see PDPTWState
 * @see Solvers#minimizeAstar(Model, java.util.function.BiConsumer)
 */
public final class PDPTWAstarMain {

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
        final PDPTWProblem problem = PDPTWGenerator.genInstance(30, 3, 5, new Random(2),true);
        Model<PDPTWState> model = new Model<>() {
            @Override
            public Problem<PDPTWState> problem() {
                return problem;
            }

            @Override
            public PDPTWFastLowerBound lowerBound() {
                return new PDPTWFastLowerBound(problem);
            }

            @Override
            public DominanceChecker<PDPTWState> dominance() {
                return new SimpleDominanceChecker<>(new PDPTWDominance(), problem.nbVars());
            }
        };

        Solution bestSolution = Solvers.minimizeAstar(model, (sol, s) -> {
            SolutionPrinter.printSolution(s, sol);
        });

        System.out.println(bestSolution.statistics());
        System.out.println(new PDPTWSolution(problem, bestSolution, -1));
    }
}
