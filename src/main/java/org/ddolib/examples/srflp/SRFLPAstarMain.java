package org.ddolib.examples.srflp;

import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.modeling.FastLowerBound;
import org.ddolib.modeling.Model;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solvers;
import org.ddolib.util.io.SolutionPrinter;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
/**
 * The Single-Row Facility Layout Problem (SRFLP) with AsTar.
 * Entry point for solving the Single-Row Facility Layout Problem (SRFLP)
 * using the A* search algorithm.
 *
 * <p>
 * <strong>Usage:</strong>
 * </p>
 * <pre>{@code
 * java SRFLPAstarMain [instanceFile]
 * }</pre>
 * If no instance file is provided as an argument, a default instance located at
 * {@code data/SRFLP/simple} will be used.
 *
 * <p>
 * The A* model requires the following components:
 * </p>
 * <ul>
 *     <li>{@link SRFLPProblem} – the problem definition (distance/cost matrix, number of facilities, etc.),</li>
 *     <li>{@link SRFLPFastLowerBound} – a fast lower-bound estimator used for pruning during the search.</li>
 * </ul>
 *
 * <p>
 * After the search is completed, the program prints:
 * </p>
 * <ul>
 *     <li>Search statistics returned by {@link Solvers#minimizeAstar},</li>
 *     <li>The best solution found as an array of facility indices.</li>
 * </ul>
 *
 * @see SRFLPProblem
 * @see SRFLPState
 * @see SRFLPFastLowerBound
 * @see Model
 * @see Solvers
 */
public class SRFLPAstarMain {
    public static void main(String[] args) throws IOException {
        final String filename = args.length == 0 ? Paths.get("data", "SRFLP", "simple").toString() :
                args[0];

        final SRFLPProblem problem = new SRFLPProblem(filename);

        Model<SRFLPState> model = new Model<>() {
            @Override
            public Problem<SRFLPState> problem() {
                return problem;
            }

            @Override
            public FastLowerBound<SRFLPState> lowerBound() {
                return new SRFLPFastLowerBound(problem);
            }
        };

        int[] bestSolution = new int[problem.nbVars()];

        SearchStatistics finalStats = Solvers.minimizeAstar(model, (sol, stat) -> {
            SolutionPrinter.printSolution(stat, sol);
            System.arraycopy(sol, 0, bestSolution, 0, sol.length);
        });

        System.out.println("\n");
        System.out.println("===== Optimal Solution =====");
        System.out.println(finalStats);
        System.out.println("Best solution: " + Arrays.toString(bestSolution));
    }
}
