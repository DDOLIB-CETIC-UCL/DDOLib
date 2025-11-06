package org.ddolib.examples.msct;

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

/**
 * Minimum Sum Completion Time (MSCT) with Acs.
 * Main class to solve an instance of the Maximum Sum of Compatible Tasks (MSCT) problem
 * using the ACS (Anytime Column Search) algorithm.
 * <p>
 * This class reads an MSCT instance from a file (default or specified via command-line argument),
 * sets up an ACS model including dominance checking and a fast lower bound, and then
 * minimizes the problem using the ACS solver.
 * </p>
 * <p>
 * The results of the search, including solutions found and statistics, are printed to
 * the standard output.
 * </p>
 *
 * <p><b>Usage:</b></p>
 * <pre>
 * java MSCTAcsMain [instance_file]
 * </pre>
 * If no {@code instance_file} is provided, a default instance located at
 * {@code data/MSCT/msct1.txt} is used.
 */
public class MSCTAcsMain {
    /**
     * Entry point for running the ACS solver on an MSCT instance.
     *
     * @param args optional command-line arguments; the first argument can specify the
     *             path to the MSCT instance file.
     * @throws IOException if reading the instance file fails
     */
    public static void main(final String[] args) throws IOException {
        final String instance = args.length == 0 ? Path.of("data","MSCT","msct1.txt").toString() : args[0];
        final MSCTProblem problem = new MSCTProblem(instance);
        AcsModel<MSCTState> model = new AcsModel<>() {
            @Override
            public Problem<MSCTState> problem() {
                return problem;
            }

            @Override
            public DominanceChecker<MSCTState> dominance() {
                return new SimpleDominanceChecker<>(new MSCTDominance(), problem.nbVars());
            }

            @Override
            public FastLowerBound<MSCTState> lowerBound() {
                return new MSCTFastLowerBound(problem);
            }
        };

        SearchStatistics stats = Solvers.minimizeAcs(model, (sol, s) -> {
            SolutionPrinter.printSolution(s,sol);
        });

        System.out.println(stats);
    }
}


