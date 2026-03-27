package org.ddolib.examples.knapsack;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.common.solver.Solution;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.modeling.*;
import org.ddolib.util.io.SolutionPrinter;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Entry point for solving the 0/1 Knapsack Problem (KS)
 * using a Large Neighborhood Search (LNS) approach combined with
 * Decision Diagram Optimization (DDO).
 *
 * <p>
 * The 0/1 Knapsack Problem consists in selecting a subset of items,
 * each with a weight and a profit, such that the total weight does not
 * exceed the knapsack capacity while maximizing the total profit.
 * Each item can be selected at most once.
 * </p>
 *
 * <p>
 * This class demonstrates how to:
 * </p>
 * <ul>
 *     <li>Load a knapsack instance from a file</li>
 *     <li>Define a {@code LnsModel} with problem-specific components</li>
 *     <li>Use dominance rules to prune suboptimal states</li>
 *     <li>Run a Large Neighborhood Search (LNS) optimization</li>
 *     <li>Print intermediate and final solutions</li>
 * </ul>
 *
 *
 * <h2>Execution</h2>
 * <p>
 * The program accepts an optional command-line argument specifying
 * the path to a knapsack instance file. If not provided, a default
 * instance is loaded from:
 * </p>
 * <pre>
 * data/Knapsack/instance_n1000_c1000_10_5_10_5_0
 * </pre>
 *
 *
 * <h2>Model Components</h2>
 * <ul>
 *     <li>{@link KSProblem} – defines the knapsack instance</li>
 *     <li>{@link KSFastLowerBound} – provides a fast lower bound estimation</li>
 *     <li>{@link KSDominance} – defines dominance relations between states</li>
 *     <li>{@link SimpleDominanceChecker} – applies dominance pruning</li>
 *     <li>{@link KSRanking} – ranks states during decision diagram compilation</li>
 *     <li>{@link FixedWidth} – limits the decision diagram width</li>
 * </ul>
 *
 * <h2>Search Configuration</h2>
 * <ul>
 *     <li>Search strategy: Large Neighborhood Search (LNS)</li>
 *     <li>Time limit: 30,000 milliseconds</li>
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
 * @see KSProblem
 * @see KSFastLowerBound
 * @see KSDominance
 * @see KSRanking
 * @see LnsModel
 * @see Solvers#minimizeLns
 * @see Solution
 */
public class KSLnsMain {

    /**
     * Main entry point of the program.
     *
     * <p>
     * Loads a knapsack instance, configures the LNS model with
     * problem-specific heuristics and dominance rules, and runs
     * the optimization process.
     * </p>
     *
     * @param args optional command-line arguments:
     *             <ul>
     *                 <li>{@code args[0]} – path to the knapsack instance file</li>
     *             </ul>
     * @throws IOException if the instance file cannot be read
     */
    public static void main(final String[] args) throws IOException {

        final String instance = args.length == 0
                ? Path.of("data", "Knapsack", "instance_n1000_c1000_10_5_10_5_0").toString()
                : args[0];

        final KSProblem problem = new KSProblem(instance);

        final LnsModel<Integer> model = new LnsModel<>() {
            @Override
            public Problem<Integer> problem() {
                return problem;
            }

            @Override
            public FastLowerBound<Integer> lowerBound() {
                return new KSFastLowerBound(problem);
            }

            @Override
            public DominanceChecker<Integer> dominance() {
                return new SimpleDominanceChecker<>(
                        new KSDominance(),
                        problem.nbVars()
                );
            }

            @Override
            public KSRanking ranking() {
                return new KSRanking();
            }

            @Override
            public WidthHeuristic<Integer> widthHeuristic() {
                return new FixedWidth<>(10);
            }
        };

        Solution bestSolution = Solvers.minimizeLns(
                model,
                s -> s.runTimeMs() < 30000,
                (sol, s) -> {
                    SolutionPrinter.printSolution(s, sol);
                }
        );

        System.out.println(bestSolution.statistics());
        System.out.println(bestSolution);
    }
}