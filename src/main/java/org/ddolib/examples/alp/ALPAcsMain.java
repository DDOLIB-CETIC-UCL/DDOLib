package org.ddolib.examples.alp;

import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.modeling.AcsModel;
import org.ddolib.modeling.Solvers;
import org.ddolib.util.io.SolutionPrinter;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * ######### Aircraft Landing Problem (ALP) #############
 * Main class to solve the <b>Aircraft Landing Problem (ALP)</b> using
 * the Anytime Column Search (ACS) algorithm.
 * <p>
 * This class demonstrates how to:
 * </p>
 * <ul>
 *   <li>Load an ALP instance from a file.</li>
 *   <li>Define an {@link AcsModel} for the problem, including the fast lower bound.</li>
 *   <li>Solve the problem using the {@link Solvers} with the ACS algorithm.</li>
 *   <li>Track and display intermediate incumbent solutions during the search.</li>
 * </ul>
 *
 * @see ALPProblem
 * @see ALPState
 * @see ALPFastLowerBound
 * @see Solvers
 * @see AcsModel
 */
public final class ALPAcsMain {

    public static void main(final String[] args) throws IOException {
        final String instance = args.length == 0 ? Path.of("data", "alp", "alp_n50_r1_c2_std10_s0").toString() : args[0];
        final ALPProblem problem = new ALPProblem(instance);
        AcsModel<ALPState> model = new AcsModel<>() {
            @Override
            public ALPProblem problem() {
                return problem;
            }

            @Override
            public ALPFastLowerBound lowerBound() {
                return new ALPFastLowerBound(problem);
            }

        };

        SearchStatistics stats = Solvers.minimizeAcs(model, (sol, s) -> {
            SolutionPrinter.printSolution(s,sol);
        });

        System.out.println(stats);
    }
}