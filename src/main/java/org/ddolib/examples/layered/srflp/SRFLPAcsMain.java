package org.ddolib.examples.layered.srflp;

import java.io.IOException;
import java.nio.file.Paths;
import org.ddolib.common.util.io.SolutionPrinter;
import org.ddolib.layered.modeling.AcsModel;
import org.ddolib.layered.modeling.FastLowerBound;
import org.ddolib.layered.modeling.Problem;
import org.ddolib.layered.modeling.Solvers;
import org.ddolib.layered.solver.Solution;

/**
 * The Single-Row Facility Layout Problem (SRFLP) with Acs. Entry point for solving the Single-Row
 * Facility Layout Problem (SRFLP) using the Anytime Column Search (ACS) algorithm.
 *
 * <p><strong>Usage:</strong>
 *
 * <pre>{@code
 * java SRFLPAcsMain [instanceFile]
 * }</pre>
 *
 * <p>If no instance file is provided as an argument, a default instance located at {@code
 * data/SRFLP/simple} will be used.
 *
 * <p>The ACS model requires the following components:
 *
 * <ul>
 *   <li>{@link SRFLPProblem} – the problem definition (distance/cost matrix, number of facilities,
 *       etc.),
 *   <li>{@link SRFLPFastLowerBound} – a fast lower-bound estimator for pruning or ranking states,
 *   <li>Optional column width for solution display formatting.
 * </ul>
 *
 * <p>After the search is completed, the program prints:
 *
 * <ul>
 *   <li>Search statistics returned by {@link Solvers#minimizeAstar},
 *   <li>The best solution found as an array of facility indices.
 * </ul>
 *
 * @see SRFLPProblem
 * @see SRFLPState
 * @see SRFLPFastLowerBound
 * @see AcsModel
 * @see Solvers
 */
public class SRFLPAcsMain {

    private SRFLPAcsMain() {}

    /**
     * Entry point of the program. Builds a {@link SRFLPProblem} instance and solves it using the
     * ACS algorithm.
     *
     * @param args optional command-line argument specifying the path to the SRFLP instance file
     * @throws IOException if there is an error reading the instance file
     */
    public static void main(String[] args) throws IOException {
        final String filename =
                args.length == 0 ? Paths.get("data", "SRFLP", "simple").toString() : args[0];

        final SRFLPProblem problem = new SRFLPProblem(filename);

        AcsModel<SRFLPState> model =
                new AcsModel<>() {
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

        Solution bestSolution =
                Solvers.minimizeAcs(
                        model,
                        (sol, stat) -> {
                            SolutionPrinter.printSolution(stat, sol);
                        });

        System.out.println("\n");
        System.out.println("===== Optimal Solution =====");
        System.out.println(bestSolution.statistics());
        System.out.println(bestSolution);
    }
}
