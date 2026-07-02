package org.ddolib.util.io;

import org.ddolib.common.solver.stat.SearchStatistics;

import java.util.Arrays;
import java.util.List;

/**
 * Utility class providing helper methods to display solutions found by a solver.
 * <p>
 * The {@code SolutionPrinter} is typically used as a callback or logging utility
 * during the search process to print newly found incumbent (best so far) solutions,
 * along with their associated search statistics.
 */
public class SolutionPrinter {
    /**
     * Prints a newly found solution and its associated statistics to the standard output.
     * <p>
     * This method is usually invoked each time a better solution is found during the solving
     * process, displaying both the solver's performance information and the variable assignment.
     *
     * @param stats    the {@link SearchStatistics} object containing information about the current search state
     * @param solution an array of integers representing the variable assignments of the new incumbent solution
     */
    public static void printSolution(SearchStatistics stats, int[] solution) {
        printFormatted(stats, Arrays.toString(solution));
    }

    public static void printSolution(SearchStatistics stats, List<Integer> solution) {
        printFormatted(stats, solution.toString());
    }

    private static void printFormatted(SearchStatistics stats, String solutionStr) {
        System.out.printf("""
                ===== New Incumbent Solution =====
                %s
                Solution: %s%n""", stats, solutionStr);
    }
}
