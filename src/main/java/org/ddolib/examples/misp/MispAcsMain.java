package org.ddolib.examples.misp;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.modeling.AcsModel;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solvers;
import org.ddolib.util.io.SolutionPrinter;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.BitSet;
/**
 * The Maximum Independent Set Problem (MISP) with Acs.
 * Entry point for solving the Maximum Independent Set Problem (MISP) using an Anytime Column Search (ACS) solver.
 * <p>
 * This class demonstrates how to configure and run an ACS solver for a MISP instance. The ACS model includes:
 * </p>
 * <ul>
 *     <li>The problem instance {@link MispProblem} read from a file.</li>
 *     <li>A dominance checker {@link SimpleDominanceChecker} using {@link MispDominance} to prune dominated states.</li>
 *     <li>A fast lower bound implementation {@link MispFastLowerBound} to guide the search.</li>
 * </ul>
 *
 * <p>
 * The solution and search statistics are printed to the standard output.
 * </p>
 */
public final class MispAcsMain {
    /**
     * Main method to execute the ACS solver on a given MISP instance.
     * <p>
     * If no command-line argument is provided, a default instance
     * <code>data/MISP/tadpole_4_2.dot</code> is used.
     * </p>
     *
     * @param args optional command-line arguments; args[0] can be the path to the MISP instance file
     * @throws IOException if there is an error reading the problem instance from the file
     */
    public static void main(String[] args) throws IOException {
        final String instance = args.length == 0 ? Path.of("data", "MISP", "tadpole_4_2.dot").toString() : args[0];
        final MispProblem problem = new MispProblem(instance);
        AcsModel<BitSet> model = new AcsModel<>() {
            @Override
            public Problem<BitSet> problem() {
                return problem;
            }

            @Override
            public DominanceChecker<BitSet> dominance() {
                return new SimpleDominanceChecker<>(new MispDominance(), problem.nbVars());
            }

            @Override
            public MispFastLowerBound lowerBound() {
                return new MispFastLowerBound(problem);
            }
        };

        SearchStatistics stats = Solvers.minimizeAcs(model, (sol, s) -> {
            SolutionPrinter.printSolution(s,sol);
        });

        System.out.println(stats);
    }

}
