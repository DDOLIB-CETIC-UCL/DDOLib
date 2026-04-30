package org.ddolib.examples.knapsack;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.common.solver.Solution;
import org.ddolib.common.solver.stopcriterion.InferenceCriterion;
import org.ddolib.modeling.AwAstarModel;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solvers;
import org.ddolib.util.io.SolutionPrinter;

import java.io.IOException;
import java.nio.file.Path;

import static org.ddolib.common.solver.stopcriterion.StopCriterion.minRelativeImprovement;

/**
 * Knapsack Problem (KS) with Anytime Weighted A* (AWA*).
 * <p>
 * This class demonstrates how to solve an instance of the Knapsack Problem (KS)
 * using the Anytime Weighted A* search algorithm.
 * </p>
 * <p>
 * The program performs the following steps:
 * </p>
 * <ol>
 *     <li>Loads a knapsack instance from a data file.</li>
 *     <li>Defines an {@link AwAstarModel} with a weight, fast lower bound, and dominance checker.</li>
 *     <li>Runs the AWA* algorithm with a stopping criterion based on relative improvement.</li>
 *     <li>Prints updates when a new incumbent solution is found.</li>
 *     <li>Outputs the final search statistics and displays a progress chart.</li>
 * </ol>
 */
public class KSAwAstarMain {
    /**
     * Entry point of the AWA* demonstration for the Knapsack Problem.
     *
     * @param args command-line arguments (optional: instance file path)
     * @throws IOException if the instance file cannot be read
     */
    public static void main(final String[] args) throws IOException {
        final String instance = args.length == 0 ? Path.of("data", "Knapsack",
                "instance_n1000_c1000_10_5_10_5_0").toString() : args[0];
        final KSProblem problem = new KSProblem(instance);

        final AwAstarModel<Integer> model = new AwAstarModel<>() {
            @Override
            public Problem<Integer> problem() {
                return problem;
            }

            @Override
            public KSFastLowerBound lowerBound() {
                return new KSFastLowerBound(problem);
            }

            @Override
            public DominanceChecker<Integer> dominance() {
                return new SimpleDominanceChecker<>(new KSDominance(), problem.nbVars());
            }

            @Override
            public double weight() {
                return 15;
            }
        };

        InferenceCriterion stop = new InferenceCriterion();

        Solution bestSolution = Solvers.minimizeAwAStar(
                model,
                minRelativeImprovement(0.01),
                (sol, s) -> {
                    SolutionPrinter.printSolution(s, sol);
                    stop.addStat(s);
                }
        );

        System.out.println(bestSolution.statistics());
        System.out.println(bestSolution);
        stop.showChart();
    }
}
