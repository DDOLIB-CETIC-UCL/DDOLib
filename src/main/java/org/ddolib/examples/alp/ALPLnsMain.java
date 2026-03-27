package org.ddolib.examples.alp;

import org.ddolib.common.solver.Solution;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.modeling.DdoModel;
import org.ddolib.modeling.LnsModel;
import org.ddolib.modeling.Solvers;
import org.ddolib.util.io.SolutionPrinter;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Entry point for solving the Aircraft Landing Problem (ALP)
 * using a Large Neighborhood Search (LNS) approach combined with
 * Decision Diagram Optimization (DDO).
 *
 * <p>
 * This class demonstrates how to:
 * </p>
 * <ul>
 *     <li>Load an ALP instance from a file</li>
 *     <li>Define a {@code LnsModel} with problem-specific components</li>
 *     <li>Run an LNS-based minimization procedure</li>
 *     <li>Print intermediate and final solutions</li>
 * </ul>
 *
 *
 * <p>
 * The Aircraft Landing Problem consists in scheduling aircraft landings
 * on a runway while respecting separation constraints and minimizing
 * deviation costs from target landing times.
 * </p>
 *
 * <p>
 * The LNS procedure iteratively explores large neighborhoods of the solution
 * space by partially relaxing the current solution and recompiling a decision
 * diagram under restricted conditions.
 * </p>
 *
 * <h2>Execution</h2>
 * <p>
 * The program expects an optional command-line argument specifying the path
 * to an ALP instance file. If no argument is provided, a default instance
 * is loaded from:
 * </p>
 * <pre>
 * data/ALP/alp_n25_r1_c2_std10_s0
 * </pre>
 *
 *
 * <h2>Model Components</h2>
 * <ul>
 *     <li>{@link ALPProblem} – defines the ALP instance</li>
 *     <li>{@link ALPFastLowerBound} – provides a fast lower bound estimation</li>
 *     <li>{@link ALPRanking} – ranks states during compilation</li>
 *     <li>{@link FixedWidth} – limits the decision diagram width</li>
 * </ul>
 *
 * <h2>Search Configuration</h2>
 * <ul>
 *     <li>Search strategy: Large Neighborhood Search (LNS)</li>
 *     <li>Time limit: 1000 milliseconds</li>
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
 *     <li>The best solution found</li>
 * </ul>
 *
 *
 * @see ALPProblem
 * @see ALPState
 * @see ALPFastLowerBound
 * @see ALPRanking
 * @see LnsModel
 * @see Solvers#minimizeLns
 * @see Solution
 */
public class ALPLnsMain {

    /**
     * Main entry point of the program.
     *
     * <p>
     * Loads an ALP instance, configures the LNS model,
     * and runs the optimization procedure.
     * </p>
     *
     * @param args optional command-line arguments:
     *             <ul>
     *                 <li>{@code args[0]} – path to the ALP instance file</li>
     *             </ul>
     * @throws IOException if the instance file cannot be read
     */
    public static void main(final String[] args) throws IOException {
        final String fileStr = args.length == 0 ?
                Path.of("data", "ALP", "alp_n25_r1_c2_std10_s0").toString() : args[0];

        final ALPProblem problem = new ALPProblem(fileStr);

        LnsModel<ALPState> model = new LnsModel<>() {
            @Override
            public ALPProblem problem() {
                return problem;
            }

            @Override
            public ALPFastLowerBound lowerBound() {
                return new ALPFastLowerBound(problem);
            }

            @Override
            public ALPRanking ranking() {
                return new ALPRanking();
            }

            @Override
            public WidthHeuristic<ALPState> widthHeuristic() {
                return new FixedWidth<>(10);
            }
        };

        Solution bestSolution = Solvers.minimizeLns(
                model,
                s -> s.runTimeMs() < 1000,
                (sol, s) -> {
                    SolutionPrinter.printSolution(s, sol);
                }
        );

        System.out.println(bestSolution.statistics());
        System.out.println(bestSolution);
    }
}
