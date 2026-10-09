package org.ddolib.examples.layered.pdptw;

import java.io.IOException;
import java.nio.file.Path;
import org.ddolib.common.solver.stat.SearchStatistics;
import org.ddolib.common.util.InvalidSolutionException;
import org.ddolib.common.util.io.SolutionPrinter;
import org.ddolib.layered.modeling.DominanceChecker;
import org.ddolib.layered.modeling.Model;
import org.ddolib.layered.modeling.Problem;
import org.ddolib.layered.modeling.SimpleDominanceChecker;
import org.ddolib.layered.modeling.Solvers;
import org.ddolib.layered.solver.Solution;

/**
 * Single Vehicle Pick-up and Delivery Problem with Time Window (PDPTW) with Ddo. Main class for
 * solving the <b>Pickup and Delivery Problem with Time Window (PDPTW)</b> using the <b>A* (A-star)
 * search algorithm</b>.
 *
 * <p>This class demonstrates how to configure and execute an A* solver for a randomly generated
 * PDPTW instance. The PDPTW involves scheduling a set of pickup and delivery tasks while minimizing
 * the overall cost or time, typically under precedence, capacity and time window constraints.
 *
 * <p><b>Execution details:</b>
 *
 * <ul>
 *   <li>A random PDPTW instance is generated using {@link PDPTWGenerator#genInstance(int, int, int,
 *       java.util.Random, Boolean)}.
 *   <li>The problem is wrapped into a {@link Model} that specifies:
 *       <ul>
 *         <li>the {@link PDPTWProblem} definition,
 *         <li>a fast lower bound through {@link PDPTWFastLowerBound} to guide A* search.
 *       </ul>
 *   <li>The solver is then launched using {@link Solvers#minimizeAstar(Model,
 *       java.util.function.BiConsumer)}.
 *   <li>Each discovered solution is printed using {@link
 *       SolutionPrinter#printSolution(SearchStatistics, int[])}.
 *   <li>Search statistics are displayed at the end of the execution.
 * </ul>
 *
 * <p><b>Usage example:</b>
 *
 * <pre>{@code
 * // Run the A* PDPTW solver
 * java PDPTWAstarMain
 *
 * // Example output:
 * Solution: [0, 2, 5, ...]
 * SearchStatistics{status=OPTIMAL, iterations=..., time=...}
 * }</pre>
 *
 * <p><b>Notes:</b>
 *
 * <ul>
 *   <li>The PDPTW instance is generated with a fixed random seed ({@code new Random(1)}) to ensure
 *       reproducible results.
 *   <li>This class serves as a demonstration of how to apply A* to combinatorial optimization
 *       within the PDPTW framework.
 * </ul>
 *
 * @see PDPTWProblem
 * @see PDPTWGenerator
 * @see PDPTWFastLowerBound
 * @see PDPTWState
 * @see Solvers#minimizeAstar(Model, java.util.function.BiConsumer)
 */
public final class PDPTWAstarMain {

    private PDPTWAstarMain() {}

    /**
     * Entry point for solving a randomly generated Pickup and Delivery Problem (PDP) instance using
     * the A* algorithm.
     *
     * <p>The instance is created with fixed parameters and solved by the A* search framework
     * provided by the {@link Solvers} utility.
     *
     * @param args optional command-line arguments (not used in this example)
     * @throws IOException if an error occurs while reading or generating the instance
     */
    public static void main(final String[] args) throws IOException {
        final String file = Path.of("data", "PDPTW", "instance_10_0").toString();
        final PDPTWProblem problem = new PDPTWProblem(file);
        System.out.println(problem.optimalValue());
        Model<PDPTWState> model =
                new Model<>() {
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

        Solution bestSolution =
                Solvers.minimizeAstar(model, (sol, s) -> SolutionPrinter.printSolution(s, sol));

        System.out.println(bestSolution.statistics());
        System.out.println(problem.optimalValue());
        try {
            System.out.println(problem.evaluate(bestSolution.solution()));
        } catch (InvalidSolutionException e) {
            throw new RuntimeException(e);
        }
    }
}
