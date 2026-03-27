package org.ddolib.examples.boundedknapsack;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.common.solver.Solution;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.modeling.LnsModel;
import org.ddolib.modeling.Solvers;
import org.ddolib.util.io.SolutionPrinter;

/**
 * Entry point for solving the Bounded Knapsack Problem (BKS)
 * using a Large Neighborhood Search (LNS) approach combined with
 * Decision Diagram Optimization (DDO).
 *
 * <p>
 * The Bounded Knapsack Problem consists in selecting quantities of items
 * (each with a profit, weight, and upper bound) such that the total weight
 * does not exceed the knapsack capacity while maximizing the total profit.
 * </p>
 *
 * <p>
 * This class demonstrates how to:
 * </p>
 * <ul>
 *     <li>Generate a synthetic BKS instance</li>
 *     <li>Define a {@code LnsModel} with problem-specific components</li>
 *     <li>Incorporate dominance rules to prune the search space</li>
 *     <li>Run a Large Neighborhood Search (LNS) optimization</li>
 *     <li>Print intermediate and final solutions</li>
 * </ul>
 *
 *
 * <h2>Instance Configuration</h2>
 * <ul>
 *     <li>Number of items: 35</li>
 *     <li>Knapsack capacity: 100</li>
 *     <li>Instance type: strongly correlated profits and weights</li>
 *     <li>Random seed: 0</li>
 * </ul>
 *
 * <h2>Model Components</h2>
 * <ul>
 *     <li>{@link BKSProblem} – defines the knapsack instance</li>
 *     <li>{@link BKSFastLowerBound} – provides a fast lower bound estimation</li>
 *     <li>{@link BKSDominance} – defines dominance relations between states</li>
 *     <li>{@link SimpleDominanceChecker} – applies dominance pruning</li>
 *     <li>{@link BKSRanking} – ranks states during decision diagram compilation</li>
 *     <li>{@link FixedWidth} – limits the decision diagram width</li>
 * </ul>
 *
 * <h2>Search Configuration</h2>
 * <ul>
 *     <li>Search strategy: Large Neighborhood Search (LNS)</li>
 *     <li>Time limit: 10,000 milliseconds</li>
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
 *     <li>The best solution found</li>
 * </ul>
 *
 *
 * @see BKSProblem
 * @see BKSFastLowerBound
 * @see BKSDominance
 * @see BKSRanking
 * @see LnsModel
 * @see Solvers#minimizeLns
 * @see Solution
 */
public class BKSLnsMain {

    /**
     * Main entry point of the program.
     *
     * <p>
     * Builds a BKS instance, configures the LNS model with
     * problem-specific heuristics and dominance rules,
     * and runs the optimization process.
     * </p>
     *
     * @param args command-line arguments (currently unused)
     */
    public static void main(String[] args) {

        final BKSProblem problem = new BKSProblem(
                35,
                100,
                BKSProblem.InstanceType.STRONGLY_CORRELATED,
                0
        );

        LnsModel<Integer> model = new LnsModel<>() {
            @Override
            public BKSProblem problem() {
                return problem;
            }

            @Override
            public BKSFastLowerBound lowerBound() {
                return new BKSFastLowerBound(problem);
            }

            @Override
            public DominanceChecker<Integer> dominance() {
                return new SimpleDominanceChecker<Integer>(
                        new BKSDominance(),
                        problem.nbVars()
                );
            }

            @Override
            public BKSRanking ranking() {
                return new BKSRanking();
            }

            @Override
            public WidthHeuristic<Integer> widthHeuristic() {
                return new FixedWidth<>(100);
            }
        };

        Solution bestSolution = Solvers.minimizeLns(
                model,
                s -> s.runTimeMs() < 10000,
                (sol, s) -> {
                    SolutionPrinter.printSolution(s, sol);
                }
        );

        System.out.println(bestSolution.statistics());
        System.out.println(bestSolution);
    }
}