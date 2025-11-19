package org.ddolib.examples.lcs;

import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.modeling.DdoModel;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solvers;
import org.ddolib.util.io.SolutionPrinter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.IntStream;

/**
 * Longest Common Subsequence (LCS) with Ddo.
 * <p>
 * Main class to solve an instance of the Longest Common Subsequence problem using
 * the Decision Diagram Optimization (DDO) algorithm.
 * </p>
 * <p>
 * This class demonstrates how to:
 * </p>
 * <ul>
 *     <li>Load an LCS problem instance from a file.</li>
 *     <li>Instantiate a {@link DdoModel} with:
 *         <ul>
 *             <li>a relaxation ({@link LCSRelax}),</li>
 *             <li>a state ranking ({@link LCSRanking}),</li>
 *             <li>a fast lower bound heuristic ({@link LCSFastLowerBound}).</li>
 *         </ul>
 *     </li>
 *     <li>Use the {@link Solvers} to minimize the objective function via DDO.</li>
 *     <li>Track and print new incumbent solutions and search statistics.</li>
 * </ul>
 *
 * <p>
 * The DDO algorithm builds and manipulates a relaxed decision diagram to efficiently
 * explore the state space and find optimal or near-optimal solutions.
 * </p>
 */
public final class LCSDdoMain {

    public static void main(String[] args) throws IOException {
        final String instance = args.length == 0 ? Path.of("src", "test", "resources", "LCS", "LCS_3_3_10_test.txt").toString() : args[0];
        final LCSProblem problem = new LCSProblem(instance);
        DdoModel<LCSState> model = new DdoModel<>() {
            @Override
            public Problem<LCSState> problem() {
                return problem;
            }

            @Override
            public LCSRelax relaxation() {
                return new LCSRelax(problem);
            }

            @Override
            public LCSRanking ranking() {
                return new LCSRanking();
            }

            @Override
            public LCSFastLowerBound lowerBound() {
                return new LCSFastLowerBound(problem);
            }
        };

        SearchStatistics stats = Solvers.minimizeDdo(model, s -> false, (sol, s) -> {
            SolutionPrinter.printSolution(s,sol);
        });
        System.out.println(stats);

    }
}