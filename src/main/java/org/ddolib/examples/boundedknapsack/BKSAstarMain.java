package org.ddolib.examples.boundedknapsack;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.common.solver.Solution;
import org.ddolib.modeling.Model;
import org.ddolib.modeling.Solvers;
import org.ddolib.util.io.SolutionPrinter;

/**
 * Bounded Knapsack Problem (BKS) with AsTar.
 * Main class for solving the Bounded Knapsack Problem (BKS) using the A* search algorithm.
 * <p>
 * This class demonstrates how to set up a BKS problem instance, define an A* model,
 * and solve the problem using the {@link Solvers#minimizeAstar(Model, java.util.function.BiConsumer)} method.
 * It also prints the solution and search statistics to the console.
 * </p>
 */
public class BKSAstarMain {
    /**
     * Entry point of the application.
     * <p>
     * The method performs the following steps:
     * </p>
     * <ol>
     *     <li>Creates a BKS problem instance with 10 items, capacity 1000, and strongly correlated instance type.</li>
     *     <li>Defines an A* model for the problem, including a lower bound and dominance checker.</li>
     *     <li>Solves the problem using the A* search algorithm.</li>
     *     <li>Prints the solution and search statistics to the console.</li>
     * </ol>
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        Model<Integer> model = new Model<>() {
            final BKSProblem problem = new BKSProblem(10, 1000, BKSProblem.InstanceType.STRONGLY_CORRELATED, 0);

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

        Solution bestSolution = Solvers.minimizeAstar(model, (sol, s) -> {
            SolutionPrinter.printSolution(s, sol);
        });

        System.out.println(bestSolution.statistics());
        System.out.println(bestSolution);
    }
}

