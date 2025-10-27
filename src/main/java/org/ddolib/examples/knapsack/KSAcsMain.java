package org.ddolib.examples.knapsack;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.modeling.AcsModel;
import org.ddolib.modeling.FastLowerBound;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solvers;
import org.ddolib.util.io.SolutionPrinter;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;

/**
 * Knapsack Problem (KS) with Acs.
 * <p>
 * This class demonstrates how to solve an instance of the Knapsack Problem (KS)
 * using the Anytime Column Search (ACS) algorithm.
 * </p>
 * <p>
 * The program performs the following steps:
 * </p>
 * <ol>
 *     <li>Loads a knapsack instance from a data file.</li>
 *     <li>Defines an {@link AcsModel} with a fast lower bound and dominance checker.</li>
 *     <li>Creates a {@link Solvers} and runs the ACS algorithm.</li>
 *     <li>Prints updates when a new incumbent solution is found.</li>
 *     <li>Outputs the final search statistics.</li>
 * </ol>
 *
 * <p>
 * The ACS solver in this example is configured to stop after 10 iterations, and
 * the column width for the ACS model is set to 10.
 * </p>
 */
public class KSAcsMain {
    /**
     * Entry point of the ACS demonstration for the Knapsack Problem.
     *
     * @param args command-line arguments (not used)
     * @throws IOException if the instance file cannot be read
     */
    public static void main(final String[] args) throws IOException {
        final String instance = args.length == 0 ?
                Path.of("data","Knapsack","instance_n1000_c1000_10_5_10_5_0").toString() :
                args[0];
        final KSProblem problem = new KSProblem(instance);
        final AcsModel<Integer> model = new AcsModel<>() {
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
                return new SimpleDominanceChecker<>(new KSDominance(), problem.nbVars());
            }

            @Override
            public int columnWidth() {
                return 10;
            }

        };

        SearchStatistics stats = Solvers.minimizeAcs(model, (sol, s) -> {
            SolutionPrinter.printSolution(s,sol);
        });

        System.out.println(stats);

    }
}
