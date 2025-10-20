package org.ddolib.examples.lcs;

import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.modeling.Model;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solvers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.IntStream;

/**
 * ######### Longest Common Subsequence (LCS) ###############
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
        final String file = "src/test/resources/LCS/LCS_3_3_10_test.txt";
        final LCSProblem problem = new LCSProblem(file);
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

        Solvers<LCSState> solver = new Solvers<>();
        SearchStatistics stats = solver.minimizeAstar(model, s -> false, (sol, s) -> {
            System.out.println("--------------------");
            System.out.println("new incumbent found " + s.incumbent() + " at iteration " + s.nbIterations());
            System.out.println("New solution: " + sol + " at iteration " + s.nbIterations());
        });
        System.out.println(stats);

    }
}