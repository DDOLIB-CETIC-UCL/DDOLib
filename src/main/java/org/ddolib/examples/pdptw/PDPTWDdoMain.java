package org.ddolib.examples.pdptw;

import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.common.solver.Solution;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.Frontier;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.modeling.DdoModel;
import org.ddolib.modeling.InvalidSolutionException;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solvers;
import org.ddolib.util.io.SolutionPrinter;

import java.io.IOException;
import java.util.Random;

import static org.ddolib.examples.pdptw.PDPTWGenerator.genInstance;

/**
 * Single Vehicle Pick-up and Delivery Problem with Time Window (PDPTW) with Ddo.
 * Main class for solving the <b>Pickup and Delivery Problem with Time Window (PDPTW)</b> using the
 * <b>Dynamic Decision Diagram Optimization (DDO)</b> approach.
 * <p>
 * This class demonstrates how to build and execute a DDO-based solver
 * for a randomly generated PDPTW instance. The PDPTW involves scheduling a set
 * of pickup and delivery tasks while minimizing the overall cost or time,
 * typically under precedence, capacity and time window constraints.
 * </p>
 *
 * <p><b>Execution workflow:</b></p>
 * <ul>
 *   <li>A random PDPTW instance is generated using
 *       {@link PDPTWGenerator#genInstance(int, int, int, java.util.Random)}.</li>
 *   <li>The instance is encapsulated in a {@link DdoModel}, which specifies:
 *     <ul>
 *       <li>the {@link PDPTWProblem} definition,</li>
 *       <li>the {@link PDPTWRelax} relaxation used to merge states in the decision diagram,</li>
 *       <li>the {@link PDPTWRanking} heuristic to prioritize state exploration,</li>
 *       <li>a {@link PDPTWFastLowerBound} estimator to guide pruning,</li>
 *       <li>a {@link SimpleFrontier} structure with {@link CutSetType#Frontier} for node management,</li>
 *       <li>a width control strategy via {@link FixedWidth} to limit the diagram size.</li>
 *     </ul>
 *   </li>
 *   <li>The DDO solver is executed through {@link Solvers#minimizeDdo(DdoModel, java.util.function.BiConsumer)}.</li>
 *   <li>Each valid solution found is displayed using {@link SolutionPrinter#printSolution(SearchStatistics, int[])}.</li>
 *   <li>Global search statistics are printed at the end of execution.</li>
 * </ul>
 *
 * <p><b>Usage example:</b></p>
 * <pre>{@code
 * // Run the DDO solver on a randomly generated PDPTW instance
 * java PDPTWDdoMain
 *
 * // Example output:
 * Solution: [0, 2, 5, 7, ...]
 * SearchStatistics{status=OPTIMAL, nodes=..., time=...}
 * }</pre>
 *
 * <p><b>Notes:</b></p>
 * <ul>
 *   <li>The PDPTW instance is generated with a fixed random seed ({@code new Random(1)})
 *       to ensure reproducible results.</li>
 *   <li>DDO explores an approximation of the full search space by iteratively refining
 *       decision diagrams, offering a good trade-off between accuracy and performance.</li>
 *   <li>Caching is enabled ({@code useCache() = true}) to avoid redundant computations
 *       across iterations.</li>
 * </ul>
 *
 * @see PDPTWProblem
 * @see PDPTWRelax
 * @see PDPTWRanking
 * @see PDPTWFastLowerBound
 * @see Solvers#minimizeDdo(DdoModel, java.util.function.BiConsumer)
 * @see DdoModel
 * @see FixedWidth
 * @see SimpleFrontier
 * @see CutSetType
 */
public final class PDPTWDdoMain {
    /**
     * Entry point for solving a randomly generated Pickup and Delivery Problem (PDPTW)
     * instance using the <b>Dynamic Decision Diagram Optimization (DDO)</b> method.
     * <p>
     * The instance is created with predefined parameters and solved using
     * a DDO model that integrates relaxation, ranking, lower bounds, and frontier
     * management heuristics.
     * </p>
     *
     * @param args optional command-line arguments (not used in this example)
     * @throws IOException if an error occurs during instance generation
     */

    public static void main(final String[] args) throws IOException, InvalidSolutionException {

        final PDPTWProblem problem = genInstance(18, 3, 10, new Random(1));
        DdoModel<PDPTWState> model = new DdoModel<>() {
            @Override
            public Problem<PDPTWState> problem() {
                return problem;
            }

            @Override
            public PDPTWFastLowerBound lowerBound() {
                return new PDPTWFastLowerBound(problem);
            }

            @Override
            public PDPTWRelax relaxation() {
                return new PDPTWRelax(problem);
            }

            @Override
            public PDPTWRanking ranking() {
                return new PDPTWRanking();
            }

            @Override
            public WidthHeuristic<PDPTWState> widthHeuristic() {
                return new FixedWidth<>(1000);
            }

            @Override
            public Frontier<PDPTWState> frontier() {
                return new SimpleFrontier<>(ranking(), CutSetType.Frontier);
            }

            @Override
            public boolean useCache() {
                return true;
            }
        };

        Solution bestSolution = Solvers.minimizeDdo(model, (sol, s) -> {
            SolutionPrinter.printSolution(s, sol);
        });

        System.out.println(bestSolution.statistics());
        problem.evaluate(bestSolution.solution());
        System.out.println(new PDPTWSolution(problem, bestSolution, -1));
    }
}
