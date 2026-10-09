package org.ddolib.examples.layered.srflp;

import java.io.IOException;
import java.nio.file.Paths;
import org.ddolib.common.util.io.SolutionPrinter;
import org.ddolib.layered.modeling.FastLowerBound;
import org.ddolib.layered.modeling.Model;
import org.ddolib.layered.modeling.Problem;
import org.ddolib.layered.modeling.Solvers;
import org.ddolib.layered.solver.Solution;

/**
 * The Single-Row Facility Layout Problem (SRFLP) with AsTar. Entry point for solving the Single-Row
 * Facility Layout Problem (SRFLP) using the A* search algorithm.
 *
 * <p><strong>Usage:</strong>
 *
 * <pre>{@code
 * java SRFLPAstarMain [instanceFile]
 * }</pre>
 *
 * <p>If no instance file is provided as an argument, a default instance located at {@code
 * data/SRFLP/simple} will be used.
 *
 * <p>The A* model requires the following components:
 *
 * <ul>
 *   <li>{@link SRFLPProblem} – the problem definition (distance/cost matrix, number of facilities,
 *       etc.),
 *   <li>{@link SRFLPFastLowerBound} – a fast lower-bound estimator used for pruning during the
 *       search.
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
 * @see Model
 * @see Solvers
 */
public class SRFLPAstarMain {

    private SRFLPAstarMain() {}

    /**
     * Entry point of the program. Builds a {@link SRFLPProblem} instance and solves it using the A*
     * algorithm.
     *
     * @param args optional command-line argument specifying the path to the SRFLP instance file
     * @throws IOException if there is an error reading the instance file
     */
    public static void main(String[] args) throws IOException {
        final String filename =
                args.length == 0 ? Paths.get("data", "SRFLP", "simple").toString() : args[0];

        final SRFLPProblem problem = new SRFLPProblem(filename);

        Model<SRFLPState> model =
                new Model<>() {
                    @Override
                    public Problem<SRFLPState> problem() {
                        return problem;
                    }

                    @Override
                    public FastLowerBound<SRFLPState> lowerBound() {
                        return new SRFLPFastLowerBound(problem);
                    }
                };

        Solution bestSolution =
                Solvers.minimizeAstar(
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
