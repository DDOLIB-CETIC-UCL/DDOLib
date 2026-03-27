package org.ddolib.examples.max2sat;

import org.ddolib.common.solver.Solution;
import org.ddolib.modeling.*;
import org.ddolib.util.io.SolutionPrinter;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Entry point for solving the Maximum 2-Satisfiability (MAX-2SAT) problem
 * using a Large Neighborhood Search (LNS) approach combined with
 * Decision Diagram Optimization (DDO).
 *
 * <p>
 * The MAX-2SAT problem consists in assigning boolean values to variables
 * in order to maximize the number (or total weight) of satisfied clauses,
 * where each clause contains at most two literals.
 * </p>
 *
 * <p>
 * This class demonstrates how to:
 * </p>
 * <ul>
 *     <li>Load a MAX-2SAT instance from a file (typically in WCNF format)</li>
 *     <li>Define a {@code LnsModel} with problem-specific components</li>
 *     <li>Use a lower bound heuristic to guide the search</li>
 *     <li>Run a Large Neighborhood Search (LNS) optimization</li>
 *     <li>Print intermediate and final solutions</li>
 * </ul>
 *
 *
 * <h2>Execution</h2>
 * <p>
 * The program accepts an optional command-line argument specifying
 * the path to a MAX-2SAT instance file. If not provided, a default
 * instance is loaded from:
 * </p>
 * <pre>
 * data/Max2Sat/wcnf_var_4_opti_39.txt
 * </pre>
 *
 *
 * <h2>Model Components</h2>
 * <ul>
 *     <li>{@link Max2SatProblem} – defines the MAX-2SAT instance</li>
 *     <li>{@link Max2SatFastLowerBound} – provides a fast lower bound on the number of satisfiable clauses</li>
 *     <li>{@link Max2SatRanking} – ranks states during decision diagram compilation</li>
 * </ul>
 *
 * <h2>Search Configuration</h2>
 * <ul>
 *     <li>Search strategy: Large Neighborhood Search (LNS)</li>
 *     <li>Time limit: 100 milliseconds</li>
 * </ul>
 *
 * <p>
 * No dominance rule or width heuristic is explicitly specified, so default
 * behaviors (if provided by the framework) are used.
 * </p>
 *
 * <h2>Output</h2>
 * <p>
 * The program prints:
 * </p>
 * <ul>
 *     <li>Intermediate solutions during the search</li>
 *     <li>Final solution statistics</li>
 *     <li>The best assignment found</li>
 * </ul>
 *
 *
 * @see Max2SatProblem
 * @see Max2SatState
 * @see Max2SatFastLowerBound
 * @see Max2SatRanking
 * @see LnsModel
 * @see Solvers#minimizeLns
 * @see Solution
 */
public class Max2SatLnsMain {

    /**
     * Main entry point of the program.
     *
     * <p>
     * Loads a MAX-2SAT instance, configures the LNS model with
     * problem-specific heuristics, and runs the optimization process.
     * </p>
     *
     * @param args optional command-line arguments:
     *             <ul>
     *                 <li>{@code args[0]} – path to the MAX-2SAT instance file</li>
     *             </ul>
     * @throws IOException if the instance file cannot be read
     */
    public static void main(String[] args) throws IOException {

        // Select instance file: default or provided path
        String instance = args.length == 0
                ? Path.of("data", "Max2Sat", "wcnf_var_4_opti_39.txt").toString()
                : args[0];

        // Load MAX2SAT instance
        final Max2SatProblem problem = new Max2SatProblem(instance);

        // Define DDO model for MAX2SAT
        LnsModel<Max2SatState> model = new LnsModel<>() {
            @Override
            public Problem<Max2SatState> problem() {
                return problem;
            }

            @Override
            public Max2SatFastLowerBound lowerBound() {
                return new Max2SatFastLowerBound(problem);
            }

            @Override
            public Max2SatRanking ranking() {
                return new Max2SatRanking();
            }
        };

        // Execute DDO search and print intermediate solutions
        Solution bestSolution = Solvers.minimizeLns(
                model,
                s -> s.runTimeMs() < 100,
                (sol, s) -> {
                    SolutionPrinter.printSolution(s, sol);
                }
        );

        // Display search statistics
        System.out.println(bestSolution.statistics());
        System.out.println(bestSolution);
    }
}