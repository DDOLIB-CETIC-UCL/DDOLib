package org.ddolib.util.verbosity;

import org.ddolib.ddo.core.SubProblem;

import java.io.*;

/**
 * Class that print details about the search given a {@link VerbosityLevel}.
 */
public class VerboseMode {


    private final VerbosityLevel verbosityLevel;
    private long nextPrint;
    private final long printInterval;

    /**
     *
     * @param verbosityLevel The level of details to print.
     * @param printInterval  The delay between two prints of statistics about the frontier
     */
    public VerboseMode(VerbosityLevel verbosityLevel, long printInterval) {
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
            try (Writer writer = getWriter()) {
                writer.append(String.format("new best: %g\n", best));
                writer.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
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
        if (verbosityLevel == VerbosityLevel.LARGE || verbosityLevel == VerbosityLevel.EXPORT) {
            try (Writer writer = getWriter()) {
                writer.append(String.format("\tit: %d\n\t\t%s\n", nbIter, sub));
                writer.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

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
        if ((verbosityLevel == VerbosityLevel.LARGE || verbosityLevel == VerbosityLevel.EXPORT) && now >= nextPrint) {

            try (Writer writer = getWriter()) {
                String msg = String.format("\tit: %d - frontier size: %d - best obj: %g - " +
                                "best in frontier: %g - gap: %g\n", nbIter, frontierSize,
                        bestObj, bestInFrontier, gap);

                writer.append(msg);
                writer.flush();
                nextPrint = now + printInterval;

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public Writer getWriter() {
        if (verbosityLevel == VerbosityLevel.EXPORT) {
            try {
                return new BufferedWriter(new FileWriter("logs.txt", true));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            return new NonClosingWriter(new PrintWriter(System.out));
        }
    }


    private static class NonClosingWriter extends FilterWriter {

        public NonClosingWriter(Writer out) {
            super(out);
        }

        @Override
        public void close() throws IOException {
        }

        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
            out.write(cbuf, off, len);
        }

        @Override
        public void flush() throws IOException {
            out.flush();
        }
    }
}
