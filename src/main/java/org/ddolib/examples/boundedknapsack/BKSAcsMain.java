package org.ddolib.examples.boundedknapsack;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.modeling.AcsModel;
import org.ddolib.modeling.Solvers;
import org.ddolib.util.io.SolutionPrinter;

/**
 * Bounded Knapsack Problem (BKS) with Acs.
 * Main class for solving the Bounded Knapsack Problem (BKS) using an Anytime Column Search (ACS) approach.
 * <p>
 * This class demonstrates how to set up a BKS problem instance, define an ACS model,
 * and solve the problem using the {@link Solvers#minimizeAcs(AcsModel, java.util.function.BiConsumer)} method.
 * It also prints the solution and statistics to the console.
 * </p>
 */
public class BKSAcsMain {
    /**
     * Entry point of the application.
     * <p>
     * The method performs the following steps:
     * </p>
     * <ol>
     *     <li>Creates a BKS problem instance with 10 items, capacity 1000, and strongly correlated instance type.</li>
     *     <li>Defines an ACS model for the problem, including a lower bound and dominance checker.</li>
     *     <li>Solves the problem using an Ant Colony System (ACS) solver.</li>
     *     <li>Prints the solution and search statistics to the console.</li>
     * </ol>
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        final BKSProblem problem = new BKSProblem(10, 1000, BKSProblem.InstanceType.STRONGLY_CORRELATED, 0);
        AcsModel<Integer> model = new AcsModel<>() {
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
                return new SimpleDominanceChecker<Integer>(new BKSDominance(), problem.nbVars());
            }
        };

        SearchStatistics stats = Solvers.minimizeAcs(model, (sol, s) -> {
            SolutionPrinter.printSolution(s,sol);
        });

        System.out.println(stats);
    }
}

