package org.ddolib.examples.alp;

import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.modeling.AcsModel;
import org.ddolib.modeling.Solver;
import java.io.IOException;
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
 *   <li>Solve the problem using the {@link Solver} with the ACS algorithm.</li>
 *   <li>Track and display intermediate incumbent solutions during the search.</li>
 * </ul>
 * @see ALPProblem
 * @see ALPState
 * @see ALPFastLowerBound
 * @see Solver
 * @see AcsModel
 */
public final class ALPAcsMain {

    public static void main(final String[] args) throws IOException {
        final String fileStr = Paths.get("data", "alp", "alp_n50_r1_c2_std10_s0").toString();
        final ALPProblem problem = new ALPProblem(fileStr);
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

        Solver<ALPState> solver = new Solver<>();

        SearchStatistics stats = solver.minimizeAcs(model, s -> false, (sol, s) -> {
            System.out.println("--------------------");
            System.out.println("new incumbent found " + s.incumbent() + " at iteration " + s.nbIterations());
            System.out.println("New solution: " + sol + " at iteration " + s.nbIterations());
        });

        System.out.println(stats);
    }
}