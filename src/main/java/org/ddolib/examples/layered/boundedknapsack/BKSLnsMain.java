package org.ddolib.examples.layered.boundedknapsack;

import org.ddolib.common.heuristics.width.FixedWidth;
import org.ddolib.common.heuristics.width.WidthHeuristic;
import org.ddolib.common.util.io.SolutionPrinter;
import org.ddolib.layered.modeling.DominanceChecker;
import org.ddolib.layered.modeling.LnsModel;
import org.ddolib.layered.modeling.SimpleDominanceChecker;
import org.ddolib.layered.modeling.Solvers;
import org.ddolib.layered.solver.Solution;

/**
 * Entry point for solving the Bounded Knapsack Problem (BKS) using a Large Neighborhood Search
 * (LNS) approach combined with Decision Diagram Optimization (DDO).
 *
 * <p>The Bounded Knapsack Problem consists in selecting quantities of items (each with a profit,
 * weight, and upper bound) such that the total weight does not exceed the knapsack capacity while
 * maximizing the total profit.
 *
 * <p>This class demonstrates how to:
 *
 * <ul>
 *   <li>Generate a synthetic BKS instance
 *   <li>Define a {@code LnsModel} with problem-specific components
 *   <li>Incorporate dominance rules to prune the search space
 *   <li>Run a Large Neighborhood Search (LNS) optimization
 *   <li>Print intermediate and final solutions
 * </ul>
 *
 * <h2>Instance Configuration</h2>
 *
 * <ul>
 *   <li>Number of items: 35
 *   <li>Knapsack capacity: 100
 *   <li>Instance type: strongly correlated profits and weights
 *   <li>Random seed: 0
 * </ul>
 *
 * <h2>Model Components</h2>
 *
 * <ul>
 *   <li>{@link BKSProblem} – defines the knapsack instance
 *   <li>{@link BKSFastLowerBound} – provides a fast lower bound estimation
 *   <li>{@link BKSDominance} – defines dominance relations between states
 *   <li>{@link SimpleDominanceChecker} – applies dominance pruning
 *   <li>{@link BKSRanking} – ranks states during decision diagram compilation
 *   <li>{@link FixedWidth} – limits the decision diagram width
 * </ul>
 *
 * <h2>Search Configuration</h2>
 *
 * <ul>
 *   <li>Search strategy: Large Neighborhood Search (LNS)
 *   <li>Time limit: 10,000 milliseconds
 *   <li>Width heuristic: fixed width of 100 nodes per layer
 * </ul>
 *
 * <h2>Output</h2>
 *
 * <p>The program prints:
 *
 * <ul>
 *   <li>Intermediate solutions during the search
 *   <li>Final solution statistics
 *   <li>The best solution found
 * </ul>
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

    private BKSLnsMain() {}

    /**
     * Main entry point of the program.
     *
     * <p>Builds a BKS instance, configures the LNS model with problem-specific heuristics and
     * dominance rules, and runs the optimization process.
     *
     * @param args command-line arguments (currently unused)
     */
    public static void main(String[] args) {

        final BKSProblem problem =
                new BKSProblem(35, 100, BKSProblem.InstanceType.STRONGLY_CORRELATED, 0);

        LnsModel<Integer> model =
                new LnsModel<>() {
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
                                new BKSDominance(), problem.nbVars());
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

        Solution bestSolution =
                Solvers.minimizeLns(
                        model,
                        s -> s.runtime() > 10000,
                        (sol, s) -> {
                            SolutionPrinter.printSolution(s, sol);
                        });

        System.out.println(bestSolution.statistics());
        System.out.println(bestSolution);
    }
}
