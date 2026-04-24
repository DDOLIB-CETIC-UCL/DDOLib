package org.ddolib.examples.pigmentscheduling;

import org.ddolib.common.solver.Solution;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.modeling.LnsModel;
import org.ddolib.modeling.Solvers;
import org.ddolib.util.io.SolutionPrinter;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Entry point for solving the Pigment Sequencing Problem (PSP)
 * using a Large Neighborhood Search (LNS) approach combined with
 * Decision Diagram Optimization (DDO).
 *
 * <p>
 * The Pigment Sequencing Problem consists in determining an optimal order
 * of pigments or items in a sequence to minimize setup times, color
 * transitions, or other sequencing costs. It arises in manufacturing,
 * printing, and similar production processes.
 * </p>
 *
 * <p>
 * This class demonstrates how to:
 * </p>
 * <ul>
 *     <li>Load a PSP instance from a file</li>
 *     <li>Define a {@code LnsModel} with problem-specific components</li>
 *     <li>Use a lower bound heuristic to guide the search</li>
 *     <li>Use a ranking heuristic to prioritize states</li>
 *     <li>Control the decision diagram width</li>
 *     <li>Run a Large Neighborhood Search (LNS) optimization</li>
 *     <li>Print intermediate and final solutions</li>
 * </ul>
 *
 *
 * <h2>Instance Configuration</h2>
 * <p>
 * The program loads a PSP instance from:
 * </p>
 * <pre>
 * data/PSP/instancesWith5items/3
 * </pre>
 * if no command-line argument is provided. Otherwise, it uses the path
 * supplied as {@code args[0]}.
 *
 *
 * <h2>Model Components</h2>
 * <ul>
 *     <li>{@link PSProblem} – defines the sequencing instance and constraints</li>
 *     <li>{@link PSFastLowerBound} – provides a fast lower bound on the objective</li>
 *     <li>{@link PSRanking} – ranks states during decision diagram compilation</li>
 *     <li>{@link FixedWidth} – limits the decision diagram width</li>
 * </ul>
 *
 * <h2>Search Configuration</h2>
 * <ul>
 *     <li>Search strategy: Large Neighborhood Search (LNS)</li>
 *     <li>Time limit: 10,000 milliseconds (10 seconds)</li>
 *     <li>Width heuristic: fixed width of 100 nodes per layer</li>
 * </ul>
 *
 * <h2>Output</h2>
 * <p>
 * The program prints:
 * </p>
 * <ul>
 *     <li>Intermediate solutions during the search</li>
 *     <li>Final solution statistics</li>
 *     <li>The best pigment sequence found</li>
 * </ul>
 *
 * @see PSProblem
 * @see PSState
 * @see PSFastLowerBound
 * @see PSRanking
 * @see LnsModel
 * @see Solvers#minimizeLns
 * @see Solution
 */
public class PSLnsMain {

    /**
     * Main entry point of the program.
     *
     * <p>
     * Loads a PSP instance, configures the LNS model with
     * problem-specific heuristics, and runs the optimization process.
     * </p>
     *
     * @param args optional command-line arguments:
     *             <ul>
     *                 <li>{@code args[0]} – path to the PSP instance file</li>
     *             </ul>
     * @throws IOException if the instance file cannot be read
     */
    public static void main(final String[] args) throws IOException {

        final String instance = args.length == 0
                ? Path.of("data", "PSP", "instancesWith5items", "3").toString()
                : args[0];

        final PSProblem problem = new PSProblem(instance);

        LnsModel<PSState> model = new LnsModel<>() {
            @Override
            public PSProblem problem() {
                return problem;
            }

            @Override
            public PSFastLowerBound lowerBound() {
                return new PSFastLowerBound(problem);
            }

            @Override
            public PSRanking ranking() {
                return new PSRanking();
            }

            @Override
            public WidthHeuristic<PSState> widthHeuristic() {
                return new FixedWidth<>(100);
            }
        };

        Solution bestSolution = Solvers.minimizeLns(
                model,
                s -> s.runtime() < 10000,
                (sol, s) -> {
                    SolutionPrinter.printSolution(s, sol);
                }
        );

        System.out.println(bestSolution.statistics());
        System.out.println(bestSolution);
    }
}