package org.ddolib.examples.layered.alp;

import org.ddolib.layered.modeling.Model;
import org.ddolib.layered.modeling.Solvers;
import org.ddolib.layered.solver.Solution;
import org.ddolib.common.util.InvalidSolutionException;
import org.ddolib.common.util.io.SolutionPrinter;

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

    private ALPAstarMain() {
    }

    /**
     * Loads an ALP instance, configures the A* model, and runs the optimization procedure.
     *
     * @param args optional path to the ALP instance file; a default instance is used if omitted
     * @throws IOException              if the instance file cannot be read
     * @throws InvalidSolutionException if the computed solution does not respect the problem's constraints
     */
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

        Solution bestSolution = Solvers.minimizeAstar(model, (sol, s) -> {
            SolutionPrinter.printSolution(s, sol);
            System.out.println(new ALPSolution(problem, sol));
        });

        System.out.println(bestSolution.statistics());
        System.out.println(bestSolution);


    }
}