package org.ddolib.examples.lcs;

import org.ddolib.common.solver.Solution;
import org.ddolib.modeling.LnsModel;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solvers;
import org.ddolib.util.io.SolutionPrinter;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Entry point for solving the Longest Common Subsequence (LCS) problem
 * using a Large Neighborhood Search (LNS) approach combined with
 * Decision Diagram Optimization (DDO).
 *
 * <p>
 * The Longest Common Subsequence problem consists in finding the longest
 * sequence of symbols that appears in the same relative order (not necessarily
 * contiguous) in a set of input sequences.
 * </p>
 *
 * <p>
 * This class demonstrates how to:
 * </p>
 * <ul>
 *     <li>Load an LCS instance from a file</li>
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
 * the path to an LCS instance file. If not provided, a default test
 * instance is loaded from:
 * </p>
 * <pre>
 * src/test/resources/LCS/LCS_3_3_10_test.txt
 * </pre>
 *
 *
 * <h2>Model Components</h2>
 * <ul>
 *     <li>{@link LCSProblem} – defines the LCS instance</li>
 *     <li>{@link LCSFastLowerBound} – provides a fast lower bound on the LCS length</li>
 *     <li>{@link LCSRanking} – ranks states during decision diagram compilation</li>
 * </ul>
 *
 * <h2>Search Configuration</h2>
 * <ul>
 *     <li>Search strategy: Large Neighborhood Search (LNS)</li>
 *     <li>Time limit: 100 milliseconds</li>
 * </ul>
 *
 * <p>
 * Note that no dominance rule or width heuristic is explicitly defined here,
 * meaning default behaviors (if any) from the underlying framework are used.
 * </p>
 *
 * <h2>Output</h2>
 * <p>
 * The program prints:
 * </p>
 * <ul>
 *     <li>Intermediate solutions during the search</li>
 *     <li>Final solution statistics</li>
 *     <li>The best subsequence found</li>
 * </ul>
 *
 * @see LCSProblem
 * @see LCSState
 * @see LCSFastLowerBound
 * @see LCSRanking
 * @see LnsModel
 * @see Solvers#minimizeLns
 * @see Solution
 */
public class LCSLnsMain {

    /**
     * Main entry point of the program.
     *
     * <p>
     * Loads an LCS instance, configures the LNS model with
     * problem-specific heuristics, and runs the optimization process.
     * </p>
     *
     * @param args optional command-line arguments:
     *             <ul>
     *                 <li>{@code args[0]} – path to the LCS instance file</li>
     *             </ul>
     * @throws IOException if the instance file cannot be read
     */
    public static void main(String[] args) throws IOException {

        final String instance = args.length == 0
                ? Path.of("src", "test", "resources", "LCS", "LCS_3_3_10_test.txt").toString()
                : args[0];

        final LCSProblem problem = new LCSProblem(instance);

        LnsModel<LCSState> model = new LnsModel<>() {
            @Override
            public Problem<LCSState> problem() {
                return problem;
            }

            @Override
            public LCSFastLowerBound lowerBound() {
                return new LCSFastLowerBound(problem);
            }

            @Override
            public LCSRanking ranking() {
                return new LCSRanking();
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