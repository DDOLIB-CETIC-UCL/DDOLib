package org.ddolib.examples.layered.msct;

import java.nio.file.Path;
import org.ddolib.common.util.io.SolutionPrinter;
import org.ddolib.layered.modeling.DominanceChecker;
import org.ddolib.layered.modeling.FastLowerBound;
import org.ddolib.layered.modeling.Model;
import org.ddolib.layered.modeling.Problem;
import org.ddolib.layered.modeling.SimpleDominanceChecker;
import org.ddolib.layered.modeling.Solvers;
import org.ddolib.layered.solver.Solution;

/**
 * Minimum Sum Completion Time (MSCT) with AsTar. Main class to solve an instance of the Maximum Sum
 * of Compatible Tasks (MSCT) problem using the A* search algorithm.
 *
 * <p>This class reads an MSCT instance from a file (default or specified via command-line
 * argument), sets up an A* model including dominance checking and a fast lower bound, and then
 * minimizes the problem using the A* solver.
 *
 * <p>The results of the search, including solutions found and statistics, are printed to the
 * standard output.
 *
 * <p><b>Usage:</b>
 *
 * <pre>
 * java MSCTAstarMain [instance_file]
 * </pre>
 *
 * <p>If no {@code instance_file} is provided, a default instance located at {@code
 * data/MSCT/msct1.txt} is used.
 */
public class MSCTAstarMain {

    private MSCTAstarMain() {}

    /**
     * Entry point for running the A* solver on an MSCT instance.
     *
     * @param args optional command-line arguments; the first argument can specify the path to the
     *     MSCT instance file.
     * @throws Exception if reading the instance file fails or any solver error occurs
     */
    public static void main(final String[] args) throws Exception {
        final String instance =
                args.length == 0 ? Path.of("data", "MSCT", "msct1.txt").toString() : args[0];
        final MSCTProblem problem = new MSCTProblem(instance);
        Model<MSCTState> model =
                new Model<>() {
                    @Override
                    public Problem<MSCTState> problem() {
                        return problem;
                    }

                    @Override
                    public FastLowerBound<MSCTState> lowerBound() {
                        return new MSCTFastLowerBound(problem);
                    }

                    @Override
                    public DominanceChecker<MSCTState> dominance() {
                        return new SimpleDominanceChecker<>(new MSCTDominance(), problem.nbVars());
                    }
                };

        Solution bestSolution =
                Solvers.minimizeAstar(
                        model,
                        (sol, s) -> {
                            SolutionPrinter.printSolution(s, sol);
                        });

        System.out.println(bestSolution.statistics());
        System.out.println(bestSolution);
    }
}
