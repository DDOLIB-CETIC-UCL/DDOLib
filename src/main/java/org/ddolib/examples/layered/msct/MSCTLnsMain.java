package org.ddolib.examples.layered.msct;

import java.io.IOException;
import java.nio.file.Path;
import org.ddolib.common.heuristics.width.FixedWidth;
import org.ddolib.common.heuristics.width.WidthHeuristic;
import org.ddolib.common.util.io.SolutionPrinter;
import org.ddolib.layered.modeling.DominanceChecker;
import org.ddolib.layered.modeling.FastLowerBound;
import org.ddolib.layered.modeling.LnsModel;
import org.ddolib.layered.modeling.Problem;
import org.ddolib.layered.modeling.SimpleDominanceChecker;
import org.ddolib.layered.modeling.Solvers;
import org.ddolib.layered.solver.Solution;

/**
 * Entry point for solving the Minimum Sum of Completion Times (MSCT) problem using a Large
 * Neighborhood Search (LNS) approach combined with Decision Diagram Optimization (DDO).
 *
 * <p>The Minimum Sum of Completion Times problem consists in scheduling a set of jobs on a single
 * machine so as to minimize the sum of their completion times. Each job has a processing time, and
 * the objective is to determine an execution order that minimizes the total completion cost.
 *
 * <p>This class demonstrates how to:
 *
 * <ul>
 *   <li>Load an MSCT instance from a file
 *   <li>Define a {@code LnsModel} with problem-specific components
 *   <li>Use a lower bound heuristic to guide the search
 *   <li>Apply dominance rules to prune suboptimal schedules
 *   <li>Control the decision diagram width
 *   <li>Run a Large Neighborhood Search (LNS) optimization
 *   <li>Print intermediate and final solutions
 * </ul>
 *
 * <h2>Execution</h2>
 *
 * <p>The program accepts an optional command-line argument specifying the path to an MSCT instance
 * file. If not provided, a default instance is loaded from:
 *
 * <pre>
 * data/MSCT/msct1.txt
 * </pre>
 *
 * <h2>Model Components</h2>
 *
 * <ul>
 *   <li>{@link MSCTProblem} – defines the scheduling instance
 *   <li>{@link MSCTFastLowerBound} – provides a fast lower bound on the objective
 *   <li>{@link MSCTDominance} – defines dominance relations between partial schedules
 *   <li>{@link SimpleDominanceChecker} – applies dominance pruning
 *   <li>{@link MSCTRanking} – ranks states during decision diagram compilation
 *   <li>{@link FixedWidth} – limits the decision diagram width
 * </ul>
 *
 * <h2>Search Configuration</h2>
 *
 * <ul>
 *   <li>Search strategy: Large Neighborhood Search (LNS)
 *   <li>Time limit: 100 milliseconds
 *   <li>Width heuristic: fixed width of 10 nodes per layer
 * </ul>
 *
 * <h2>Output</h2>
 *
 * <p>The program prints:
 *
 * <ul>
 *   <li>Intermediate solutions during the search
 *   <li>Final solution statistics
 *   <li>The best job sequence found
 * </ul>
 *
 * @see MSCTProblem
 * @see MSCTState
 * @see MSCTFastLowerBound
 * @see MSCTDominance
 * @see MSCTRanking
 * @see LnsModel
 * @see Solvers#minimizeLns
 * @see Solution
 */
public class MSCTLnsMain {

    private MSCTLnsMain() {}

    /**
     * Main entry point of the program.
     *
     * <p>Loads an MSCT instance, configures the LNS model with problem-specific heuristics and
     * dominance rules, and runs the optimization process.
     *
     * @param args optional command-line arguments:
     *     <ul>
     *       <li>{@code args[0]} – path to the MSCT instance file
     *     </ul>
     *
     * @throws IOException if the instance file cannot be read
     */
    public static void main(final String[] args) throws IOException {

        final String instance =
                args.length == 0 ? Path.of("data", "MSCT", "msct1.txt").toString() : args[0];

        final MSCTProblem problem = new MSCTProblem(instance);

        LnsModel<MSCTState> model =
                new LnsModel<>() {
                    @Override
                    public Problem<MSCTState> problem() {
                        return problem;
                    }

                    @Override
                    public FastLowerBound<MSCTState> lowerBound() {
                        return new MSCTFastLowerBound(problem);
                    }

                    @Override
                    public DominanceChecker<MSCTState> dominance() {
                        return new SimpleDominanceChecker<>(new MSCTDominance(), problem.nbVars());
                    }

                    @Override
                    public MSCTRanking ranking() {
                        return new MSCTRanking();
                    }

                    @Override
                    public WidthHeuristic<MSCTState> widthHeuristic() {
                        return new FixedWidth<>(10);
                    }
                };

        Solution bestSolution =
                Solvers.minimizeLns(
                        model,
                        s -> s.runtime() > 1000,
                        (sol, s) -> {
                            SolutionPrinter.printSolution(s, sol);
                        });

        System.out.println(bestSolution.statistics());
        System.out.println(bestSolution);
    }
}
