package org.ddolib.examples.lcs;

import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.modeling.DdoModel;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.IntStream;

/**
 * ############# Longest Common Subsequence (LCS) ###############
 */
public final class LCSDdoMain {

    public static void main(String[] args) throws IOException {
        final String file = "src/test/resources/LCS/LCS_3_3_10_test.txt";
        final LCSProblem problem = new LCSProblem(file);
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

        Solver<LCSState> solver = new Solver<>();
        SearchStatistics stats = solver.minimizeDdo(model, s -> false, (sol, s) -> {
            System.out.println("--------------------");
            System.out.println("new incumbent found " + s.incumbent() + " at iteration " + s.nbIterations());
            System.out.println("New solution: " + sol + " at iteration " + s.nbIterations());
        });
        System.out.println(stats);

    }
}