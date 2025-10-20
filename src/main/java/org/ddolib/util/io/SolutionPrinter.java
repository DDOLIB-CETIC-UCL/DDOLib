package org.ddolib.util.io;

import org.ddolib.common.solver.SearchStatistics;

import java.util.Arrays;

public class SolutionPrinter {

    public static void printSolution(SearchStatistics stats, int [] solution) {
        System.out.println("===== New Incumbent Solution =====");
        System.out.println(stats);
        System.out.println("Solution:" + Arrays.toString(solution));
    }
}
