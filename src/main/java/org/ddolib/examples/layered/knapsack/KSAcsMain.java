package org.ddolib.examples.layered.knapsack;

import java.io.IOException;
import java.nio.file.Path;
import org.ddolib.common.util.io.SolutionPrinter;
import org.ddolib.layered.modeling.AcsModel;
import org.ddolib.layered.modeling.DominanceChecker;
import org.ddolib.layered.modeling.FastLowerBound;
import org.ddolib.layered.modeling.Problem;
import org.ddolib.layered.modeling.SimpleDominanceChecker;
import org.ddolib.layered.modeling.Solvers;
import org.ddolib.layered.solver.Solution;

/**
 * Knapsack Problem (KS) with Acs.
 *
 * <p>This class demonstrates how to solve an instance of the Knapsack Problem (KS) using the
 * Anytime Column Search (ACS) algorithm.
 *
 * <p>The program performs the following steps:
 *
 * <ol>
 *   <li>Loads a knapsack instance from a data file.
 *   <li>Defines an {@link AcsModel} with a fast lower bound and dominance checker.
 *   <li>Creates a {@link Solvers} and runs the ACS algorithm.
 *   <li>Prints updates when a new incumbent solution is found.
 *   <li>Outputs the final search statistics.
 * </ol>
 *
 * <p>The ACS solver in this example is configured to stop after 10 iterations, and the column width
 * for the ACS model is set to 10.
 */
public class KSAcsMain {

    private KSAcsMain() {}

    /**
     * Entry point of the ACS demonstration for the Knapsack Problem.
     *
     * @param args command-line arguments (not used)
     * @throws IOException if the instance file cannot be read
     */
    public static void main(final String[] args) throws IOException {
        final String instance =
                args.length == 0
                        ? Path.of("data", "Knapsack", "instance_n1000_c1000_10_5_10_5_0").toString()
                        : args[0];
        final KSProblem problem = new KSProblem(instance);

        final AcsModel<Integer> model =
                new AcsModel<>() {
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

        Solution bestSolution =
                Solvers.minimizeAcs(
                        model,
                        (sol, s) -> {
                            SolutionPrinter.printSolution(s, sol);
                        });

        System.out.println(bestSolution.statistics());
        System.out.println(bestSolution);
    }
}
