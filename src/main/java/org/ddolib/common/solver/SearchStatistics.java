package org.ddolib.common.solver;

import org.ddolib.util.PrettyPrint;

/**
 * Immutable snapshot of the solver progress at a given iteration.
 *
 * @param status search status at snapshot time
 * @param nbIterations number of explored iterations
 * @param queueMaxSize maximum queue size observed so far
 * @param runTimeMs elapsed runtime in milliseconds
 * @param incumbent current incumbent objective value
 * @param gap relative optimality gap (in percent)
 */
public record SearchStatistics(
        SearchStatus status,
        int nbIterations,
        int queueMaxSize,
        long runTimeMs,
        double incumbent,
        double gap) {

    /**
     * Returns a semicolon-separated representation of this snapshot.
     *
     * @return CSV-compatible line containing all record fields
     */
    public String toCSV() {
        return String.format("%s;%d;%d;%d;%f;%f",
                status,
                nbIterations,
                queueMaxSize,
                runTimeMs,
                incumbent,
                gap);
    }

    @Override
    public String toString() {
        return "\n\tstatus = " + status +
                "\n\tnbIterations = " + nbIterations +
                "\n\tqueueMaxSize = " + queueMaxSize +
                "\n\trunTimeMs = " + PrettyPrint.formatMs(runTimeMs) +
                "\n\tincumbent = " + (incumbent == Double.POSITIVE_INFINITY || incumbent == Double.NEGATIVE_INFINITY ? "+-∞" : incumbent) +
                "\n\tgap = " + (gap == Double.POSITIVE_INFINITY ? "∞" : gap) +
                "\n";
    }
}

