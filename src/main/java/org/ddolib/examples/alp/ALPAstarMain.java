package org.ddolib.examples.alp;

import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.modeling.Model;
import org.ddolib.modeling.Solvers;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * ######### Aircraft Landing Problem (ALP) #############
 * Main class to solve the <b>Aircraft Landing Problem (ALP)</b> using
 * the A* search algorithm.
 * <p>
 * This class demonstrates how to:
 * </p>
 * <ul>
 *   <li>Load an ALP instance from a data file.</li>
 *   <li>Define a {@link Model} for the problem, including the fast lower bound.</li>
 *   <li>Solve the problem using the {@link Solvers} with the A* algorithm.</li>
 *   <li>Monitor and print intermediate incumbent solutions found during the search.</li>
 * </ul>
 *
 * @see ALPProblem
 * @see ALPState
 * @see ALPFastLowerBound
 * @see Solvers
 * @see Model
 */
public final class ALPAstarMain {

    public static void main(final String[] args) throws IOException {
        final String fileStr = Paths.get("data", "alp", "alp_n50_r1_c2_std10_s0").toString();
        final ALPProblem problem = new ALPProblem(fileStr);
        Model<ALPState> model = new Model<>() {
            @Override
            public ALPProblem problem() {
                return problem;
            }

            @Override
            public ALPFastLowerBound lowerBound() {
                return new ALPFastLowerBound(problem);
            }

        };

        SearchStatistics stats = Solvers.minimizeAstar(model, s -> false, (sol, s) -> {
            System.out.println("--------------------");
            System.out.println("new incumbent found " + s.incumbent() + " at iteration " + s.nbIterations());
            System.out.println("New solution: " + sol + " at iteration " + s.nbIterations());
        });

        System.out.println(stats);


    }
}