package org.ddolib.util;

import org.ddolib.ddo.core.SubProblem;
import org.ddolib.modeling.VerbosityLevel;

/**
 * Class that print details about the search given a {@link VerbosityLevel}.
 */
public class VerbosityPrinter {


    private final VerbosityLevel verbosityLevel;
    private long nextPrint;
    private final long printInterval;

    /**
     *
     * @param verbosityLevel The level of details to print.
     * @param printInterval  The delay between two prints of statistics about the frontier
     */
    public VerbosityPrinter(VerbosityLevel verbosityLevel, long printInterval) {
        this.verbosityLevel = verbosityLevel;
        this.printInterval = printInterval;
        nextPrint = System.currentTimeMillis() + printInterval;
    }

    /**
     * Prints message when a new best solution is found.
     *
     * @param best The value of the new best solution.
     */
    public void newBest(double best) {
        if (verbosityLevel != VerbosityLevel.SILENT) {
            System.out.printf("new best: %g%n", best);
        }
    }

    /**
     * Prints message describing the current explored sub problem.
     *
     * @param nbIter The current iteration number.
     * @param sub    The current sub problem to explore.
     * @param <T>    The type of the state.
     */
    public <T> void currentSubProblem(int nbIter, SubProblem<T> sub) {
        if (verbosityLevel == VerbosityLevel.LARGE) {
            String msg = String.format("\tit: %d\n\t\t%s", nbIter, sub);
            System.out.println(msg);
        }
    }

    /**
     * Prints statistics about the frontier after every half second.
     *
     * @param nbIter         The current iteration number.
     * @param frontierSize   The current size of the frontier.
     * @param bestObj        The current best objective value.
     * @param bestInFrontier The best value in the frontier
     * @param gap            The current gap0
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
