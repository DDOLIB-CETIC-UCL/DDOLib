package org.ddolib.examples.srflp;

import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.modeling.AcsModel;
import org.ddolib.modeling.FastLowerBound;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solvers;
import org.ddolib.util.io.SolutionPrinter;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
/**
 * The Single-Row Facility Layout Problem (SRFLP) with Acs.
 * Entry point for solving the Single-Row Facility Layout Problem (SRFLP)
 * using the Anytime Column Search (ACS) algorithm.
 *
 * <p>
 * <strong>Usage:</strong>
 * </p>
 * <pre>{@code
 * java SRFLPAcsMain [instanceFile]
 * }</pre>
 * If no instance file is provided as an argument, a default instance located at
 * {@code data/SRFLP/simple} will be used.
 *
 *
 * <p>
 * The ACS model requires the following components:
 * </p>
 * <ul>
 *     <li>{@link SRFLPProblem} – the problem definition (distance/cost matrix, number of facilities, etc.),</li>
 *     <li>{@link SRFLPFastLowerBound} – a fast lower-bound estimator for pruning or ranking states,</li>
 *     <li>Optional column width for solution display formatting.</li>
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
 * @see AcsModel
 * @see Solvers
 */
public class SRFLPAcsMain {
    public static void main(String[] args) throws IOException {
        final String filename = args.length == 0 ? Paths.get("data", "SRFLP", "simple").toString() :
                args[0];

        final SRFLPProblem problem = new SRFLPProblem(filename);

        AcsModel<SRFLPState> model = new AcsModel<>() {
            @Override
            public Problem<SRFLPState> problem() {
                return problem;
            }

            @Override
            public FastLowerBound<SRFLPState> lowerBound() {
                return new SRFLPFastLowerBound(problem);
            }

            @Override
            public int columnWidth() {
                return 50;
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
