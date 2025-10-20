package org.ddolib.examples.knapsack;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.modeling.FastLowerBound;
import org.ddolib.modeling.Model;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solvers;

import java.io.IOException;

/**
 * ######### Knapsack Problem (KS) - A* Example #############
 * <p>
 * This class demonstrates how to solve an instance of the Knapsack Problem (KS)
 * using the A* search algorithm.
 * </p>
 * <p>
 * The program performs the following steps:
 * </p>
 * <ol>
 *     <li>Loads a knapsack instance from a data file.</li>
 *     <li>Defines a {@link Model} with a fast lower bound and a dominance checker.</li>
 *     <li>Creates a {@link Solvers} and runs the A* algorithm.</li>
 *     <li>Prints updates when a new incumbent solution is found.</li>
 *     <li>Outputs the final search statistics.</li>
 * </ol>
 *
 * <p>
 * The A* solver uses the specified dominance checker and fast lower bound to prune
 * the search tree and guide the exploration efficiently.
 * </p>
 */
public class KSAstarMain {
    /**
     * Entry point of the A* demonstration for the Knapsack Problem.
     *
     * @param args command-line arguments (not used)
     * @throws IOException if the instance file cannot be read
     */
    public static void main(final String[] args) throws IOException {
        final String instance = "data/Knapsack/instance_n1000_c1000_10_5_10_5_0";
        final KSProblem problem = new KSProblem(instance);
        final Model<Integer> model = new Model<>() {
            @Override
            public Problem<Integer> problem() {
                return problem;
            }

            @Override
            public DominanceChecker<Integer> dominance() {
                return new SimpleDominanceChecker<>(new KSDominance(), problem.nbVars());
            }

            @Override
            public FastLowerBound<Integer> lowerBound() {
                return new KSFastLowerBound(problem);
            }
        };

        SearchStatistics stats = Solvers.minimizeAstar(model, s -> false, (sol, s) -> {
            System.out.println("--------------------");
            System.out.println("new incumbent found " + s.incumbent() + " at iteration " + s.nbIterations());
            System.out.println("New solution: " + sol + " at iteration " + s.nbIterations());
        });

        System.out.println(stats);

    }

}
