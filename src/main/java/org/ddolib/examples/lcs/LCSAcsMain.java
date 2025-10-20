package org.ddolib.examples.lcs;

import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.modeling.AcsModel;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solvers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.IntStream;

/**
 * ######### Longest Common Subsequence (LCS) ###############
 * <p>
 * Main class to solve an instance of the Longest Common Subsequence problem using
 * the Anytime Column Search (ACS) algorithm.
 * </p>
 * <p>
 * This class demonstrates how to:
 * </p>
 * <ul>
 *     <li>Load an LCS problem instance from a file.</li>
 *     <li>Instantiate an {@link AcsModel} with a fast lower bound heuristic.</li>
 *     <li>Use the {@link Solvers} to minimize the objective function via ACS.</li>
 *     <li>Track and print new incumbent solutions and search statistics.</li>
 * </ul>
 * <p>
 * The ACS algorithm iteratively explores the state space, updating the best solution
 * found while optionally applying a stopping criterion.
 * </p>
 */
public final class LCSAcsMain {

    public static void main(String[] args) throws IOException {
        final String instance = args.length == 0 ? Path.of("src", "test", "resources", "LCS", "LCS_3_3_10_test.txt").toString() : args[0];
        final LCSProblem problem = new LCSProblem(instance);
        AcsModel<LCSState> model = new AcsModel<>() {
            @Override
            public Problem<LCSState> problem() {
                return problem;
            }

            @Override
            public LCSFastLowerBound lowerBound() {
                return new LCSFastLowerBound(problem);
            }
        };

        SearchStatistics stats = Solvers.minimizeAcs(model, s -> false, (sol, s) -> {
            System.out.println("--------------------");
            System.out.println("new incumbent found " + s.incumbent() + " at iteration " + s.nbIterations());
            System.out.println("New solution: " + sol + " at iteration " + s.nbIterations());
        });
        System.out.println(stats);

    }
}