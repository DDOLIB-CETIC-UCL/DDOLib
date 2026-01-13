package org.ddolib.examples.lcs;

import org.ddolib.common.solver.Solution;
import org.ddolib.modeling.Model;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solvers;
import org.ddolib.util.io.SolutionPrinter;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Longest Common Subsequence (LCS) with AsTar.
 * <p>
 * Main class to solve an instance of the Longest Common Subsequence problem using
 * the A* search algorithm.
 * </p>
 * <p>
 * This class demonstrates how to:
 * </p>
 * <ul>
 *     <li>Load an LCS problem instance from a file.</li>
 *     <li>Instantiate a {@link Model} with a fast lower bound heuristic.</li>
 *     <li>Use the {@link Solvers} to minimize the objective function via A* search.</li>
 *     <li>Track and print new incumbent solutions and search statistics.</li>
 * </ul>
 *
 * <p>
 * The A* algorithm systematically explores the state space, expanding nodes in an
 * order guided by the lower bound heuristic to efficiently find optimal solutions.
 * </p>
 */
public final class LCSAstarMain {

    public static void main(String[] args) throws IOException {
        final String instance = args.length == 0 ? Path.of("src", "test", "resources", "LCS", "LCS_3_3_10_test.txt").toString() : args[0];
        final LCSProblem problem = new LCSProblem(instance);
        Model<LCSState> model = new Model<>() {
            @Override
            public Problem<LCSState> problem() {
                return problem;
            }

            @Override
            public LCSFastLowerBound lowerBound() {
                return new LCSFastLowerBound(problem);
            }
        };

        Solution bestSolution = Solvers.minimizeAstar(model, s -> false, (sol, s) -> {
            SolutionPrinter.printSolution(s, sol);
        });
        System.out.println(bestSolution.statistics());
        System.out.println(bestSolution);

    }
}