package org.ddolib.examples.msct;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.common.solver.Solution;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.modeling.FastLowerBound;
import org.ddolib.modeling.LnsModel;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solvers;
import org.ddolib.util.io.SolutionPrinter;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Entry point for solving the Minimum Sum of Completion Times (MSCT) problem
 * using a Large Neighborhood Search (LNS) approach combined with
 * Decision Diagram Optimization (DDO).
 *
 * <p>
 * The Minimum Sum of Completion Times problem consists in scheduling a set
 * of jobs on a single machine so as to minimize the sum of their completion times.
 * Each job has a processing time, and the objective is to determine an execution
 * order that minimizes the total completion cost.
 * </p>
 *
 * <p>
 * This class demonstrates how to:
 * </p>
 * <ul>
 *     <li>Load an MSCT instance from a file</li>
 *     <li>Define a {@code LnsModel} with problem-specific components</li>
 *     <li>Use a lower bound heuristic to guide the search</li>
 *     <li>Apply dominance rules to prune suboptimal schedules</li>
 *     <li>Control the decision diagram width</li>
 *     <li>Run a Large Neighborhood Search (LNS) optimization</li>
 *     <li>Print intermediate and final solutions</li>
 * </ul>
 *
 *
 * <h2>Execution</h2>
 * <p>
 * The program accepts an optional command-line argument specifying
 * the path to an MSCT instance file. If not provided, a default
 * instance is loaded from:
 * </p>
 * <pre>
 * data/MSCT/msct1.txt
 * </pre>
 *
 *
 * <h2>Model Components</h2>
 * <ul>
 *     <li>{@link MSCTProblem} – defines the scheduling instance</li>
 *     <li>{@link MSCTFastLowerBound} – provides a fast lower bound on the objective</li>
 *     <li>{@link MSCTDominance} – defines dominance relations between partial schedules</li>
 *     <li>{@link SimpleDominanceChecker} – applies dominance pruning</li>
 *     <li>{@link MSCTRanking} – ranks states during decision diagram compilation</li>
 *     <li>{@link FixedWidth} – limits the decision diagram width</li>
 * </ul>
 *
 * <h2>Search Configuration</h2>
 * <ul>
 *     <li>Search strategy: Large Neighborhood Search (LNS)</li>
 *     <li>Time limit: 100 milliseconds</li>
 *     <li>Width heuristic: fixed width of 10 nodes per layer</li>
 * </ul>
 *
 * <h2>Output</h2>
 * <p>
 * The program prints:
 * </p>
 * <ul>
 *     <li>Intermediate solutions during the search</li>
 *     <li>Final solution statistics</li>
 *     <li>The best job sequence found</li>
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

    /**
     * Main entry point of the program.
     *
     * <p>
     * Loads an MSCT instance, configures the LNS model with
     * problem-specific heuristics and dominance rules, and runs
     * the optimization process.
     * </p>
     *
     * @param args optional command-line arguments:
     *             <ul>
     *                 <li>{@code args[0]} – path to the MSCT instance file</li>
     *             </ul>
     * @throws IOException if the instance file cannot be read
     */
    public static void main(final String[] args) throws IOException {

        final String instance = args.length == 0
                ? Path.of("data", "MSCT", "msct1.txt").toString()
                : args[0];

        final MSCTProblem problem = new MSCTProblem(instance);

        LnsModel<MSCTState> model = new LnsModel<>() {
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
                return new SimpleDominanceChecker<>(
                        new MSCTDominance(),
                        problem.nbVars()
                );
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

        Solution bestSolution = Solvers.minimizeLns(
                model,
                s -> s.runtime() < 100,
                (sol, s) -> {
                    SolutionPrinter.printSolution(s, sol);
                }
        );

        System.out.println(bestSolution.statistics());
        System.out.println(bestSolution);
    }
}