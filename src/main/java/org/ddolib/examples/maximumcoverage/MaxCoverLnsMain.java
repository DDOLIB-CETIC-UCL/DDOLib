package org.ddolib.examples.maximumcoverage;

import org.ddolib.common.solver.Solution;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.modeling.LnsModel;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solvers;
import org.ddolib.util.io.SolutionPrinter;

import java.io.IOException;

/**
 * Entry point for solving the Maximum Coverage (MaxCover) problem
 * using a Large Neighborhood Search (LNS) approach combined with
 * Decision Diagram Optimization (DDO).
 *
 * <p>
 * The Maximum Coverage problem consists in selecting at most {@code k} subsets
 * from a collection such that the number of covered elements is maximized.
 * Each subset covers a portion of the universe, and the objective is to maximize
 * the union of the selected subsets.
 * </p>
 *
 * <p>
 * This class demonstrates how to:
 * </p>
 * <ul>
 *     <li>Generate or load a MaxCover instance</li>
 *     <li>Define a {@code LnsModel} with problem-specific components</li>
 *     <li>Use a ranking heuristic and a lower bound to guide the search</li>
 *     <li>Control the decision diagram width</li>
 *     <li>Run a Large Neighborhood Search (LNS) optimization</li>
 *     <li>Print intermediate and final solutions</li>
 * </ul>
 *
 *
 * <h2>Instance Configuration</h2>
 * <p>
 * By default, a random instance is generated with:
 * </p>
 * <ul>
 *     <li>30 elements (universe size)</li>
 *     <li>30 subsets</li>
 *     <li>Maximum of 7 subsets selected</li>
 *     <li>Density parameter: 0.1</li>
 *     <li>Random seed: 42</li>
 * </ul>
 * Alternative constructors (commented in the code) allow using smaller
 * instances or loading from a file.
 *
 *
 * <h2>Model Components</h2>
 * <ul>
 *     <li>{@link MaxCoverProblem} – defines the MaxCover instance</li>
 *     <li>{@link MaxCoverRanking} – ranks states during decision diagram compilation</li>
 *     <li>{@link MaxCoverFastLowerBound} – provides a fast lower bound on coverage</li>
 *     <li>{@link FixedWidth} – limits the decision diagram width</li>
 * </ul>
 *
 * <h2>Search Configuration</h2>
 * <ul>
 *     <li>Search strategy: Large Neighborhood Search (LNS)</li>
 *     <li>Time limit: 1000 milliseconds</li>
 *     <li>Width heuristic: fixed width of 1000 nodes per layer</li>
 *     <li>DOT export: disabled</li>
 * </ul>
 *
 * <p>
 * No dominance rule is explicitly defined, so default behavior (if any)
 * from the framework is used.
 * </p>
 *
 * <h2>Output</h2>
 * <p>
 * The program prints:
 * </p>
 * <ul>
 *     <li>The generated problem instance</li>
 *     <li>Intermediate solutions during the search</li>
 *     <li>The final best solution found</li>
 * </ul>
 *
 * @see MaxCoverProblem
 * @see MaxCoverState
 * @see MaxCoverRanking
 * @see MaxCoverFastLowerBound
 * @see LnsModel
 * @see Solvers#minimizeLns
 * @see Solution
 */
public class MaxCoverLnsMain {

    /**
     * Main entry point of the program.
     *
     * <p>
     * Builds or loads a Maximum Coverage instance, configures the LNS model
     * with problem-specific heuristics, and runs the optimization process.
     * </p>
     *
     * @param args command-line arguments (currently unused)
     * @throws IOException if an input file is used and cannot be read
     */
    public static void main(String[] args) throws IOException {

        MaxCoverProblem problem = new MaxCoverProblem(30, 30, 7, 0.1, 42);
        // MaxCoverProblem problem = new MaxCoverProblem(10, 10, 5,0.1,42);
        // MaxCoverProblem problem = new MaxCoverProblem("src/test/resources/MaxCover/mc_n10_m5_k3_r10_0.txt");

        System.out.println(problem);

        LnsModel<MaxCoverState> model = new LnsModel<>() {
            @Override
            public Problem<MaxCoverState> problem() {
                return problem;
            }

            @Override
            public MaxCoverFastLowerBound lowerBound() {
                return new MaxCoverFastLowerBound(problem);
            }

            @Override
            public MaxCoverRanking ranking() {
                return new MaxCoverRanking();
            }

            @Override
            public WidthHeuristic<MaxCoverState> widthHeuristic() {
                return new FixedWidth<>(1000);
            }

            @Override
            public boolean exportDot() {
                return false;
            }
        };

        Solution solution = Solvers.minimizeLns(
                model,
                s -> s.runtime() < 1000,
                (sol, s) -> {
                    SolutionPrinter.printSolution(s, sol);
                }
        );

        System.out.println();
        System.out.println(solution);
    }
}