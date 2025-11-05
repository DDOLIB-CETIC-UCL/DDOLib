package org.ddolib.examples.alp;

import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.modeling.InvalidSolutionException;
import org.ddolib.modeling.Model;
import org.ddolib.modeling.Solvers;
import org.ddolib.util.io.SolutionPrinter;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Aircraft Landing Problem (ALP) with AsTar.
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

    public static void main(final String[] args) throws IOException, InvalidSolutionException {
        final String fileStr = args.length == 0 ?
                Path.of("data", "ALP", "alp_n50_r1_c2_std10_s0").toString() : args[0];
        final ALPProblem problem = new ALPProblem("src/test/resources/ALP/ALP_10_2_2_test.txt");
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

        SearchStatistics stats = Solvers.minimizeAstar(model, (sol, s) -> {
            SolutionPrinter.printSolution(s, sol);
            System.out.println(new ALPSolution(problem, sol));
        });

        System.out.println(stats);


    }
}