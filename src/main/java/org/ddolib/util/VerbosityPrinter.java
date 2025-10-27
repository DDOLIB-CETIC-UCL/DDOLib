package org.ddolib.util;

import org.ddolib.ddo.core.SubProblem;
import org.ddolib.modeling.VerbosityLevel;

/**
 * Utility class for printing detailed information about the search process
 * based on a specified {@link VerbosityLevel}.
 * <p>
 * Depending on the verbosity level, this class can print:
 * <ul>
 *     <li>No output (SILENT)</li>
 *     <li>New best solution values (NORMAL)</li>
 *     <li>New best solution values, frontier statistics, and details about
 *     each explored subproblem (LARGE)</li>
 * </ul>
 */
public class VerbosityPrinter {


    private final VerbosityLevel verbosityLevel;
    private long nextPrint;
    private final long printInterval;

    /**
     * Creates a {@code VerbosityPrinter} instance with a given verbosity level
     * and interval for printing frontier statistics.
     *
     * @param verbosityLevel The level of details to print.
     * @param printInterval  The minimum delay (in milliseconds) between
     *                       consecutive prints of frontier statistics.
     */
    public VerbosityPrinter(VerbosityLevel verbosityLevel, long printInterval) {
        this.verbosityLevel = verbosityLevel;
        this.printInterval = printInterval;
        nextPrint = System.currentTimeMillis() + printInterval;
    }

    /**
     * Prints a message when a new best solution is found.
     *
     * @param best The value of the new best solution.
     */
    public void newBest(double best) {
        if (verbosityLevel != VerbosityLevel.SILENT) {
            System.out.printf("new best: %g%n", best);
        }
    }

    /**
     * Prints details of the currently explored subproblem.
     *
     * @param nbIter The current iteration number.
     * @param sub    The current subproblem being explored.
     * @param <T>    The type of the state of the subproblem.
     */
    public <T> void currentSubProblem(int nbIter, SubProblem<T> sub) {
        if (verbosityLevel == VerbosityLevel.LARGE) {
            String msg = String.format("\tit: %d\n\t\t%s", nbIter, sub);
            System.out.println(msg);
        }
    }

    /**
     * Prints detailed statistics about the search frontier if the specified
     * print interval has elapsed since the last print.
     *
     * @param nbIter         The current iteration number.
     * @param frontierSize   The number of subproblems in the frontier.
     * @param bestObj        The current best objective value found.
     * @param bestInFrontier The best lower bound in the frontier.
     * @param gap            The current relative gap between best objective
     *                       and best in frontier, in percent.
     */
    public void detailedSearchState(int nbIter, int frontierSize, double bestObj,
                                    double bestInFrontier, double gap) {
        long now = System.currentTimeMillis();
        if (verbosityLevel == VerbosityLevel.LARGE && now >= nextPrint) {
            String msg = String.format("\tit: %d - frontier size: %d - best obj: %g - " +
                            "best in frontier: %g - gap: %g%n", nbIter, frontierSize,
                    bestObj, bestInFrontier, gap);
            System.out.println(msg);
            nextPrint = now + printInterval;
        }
    }

}
