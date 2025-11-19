package org.ddolib.examples.alp;

import org.ddolib.modeling.StateRanking;

import java.util.Arrays;

/**
 * Ranking heuristic for {@link ALPState} objects in the Aircraft Landing Problem (ALP).
 * <p>
 * This ranking is used to prioritize states when building decision diagrams or exploring
 * the search space. The comparison is done in two steps:
 * </p>
 * <ol>
 *     <li>Compare the total number of remaining aircraft to land across all classes.</li>
 *     <li>If the remaining aircraft count is equal, compare the sum of previous landing times
 *         on all runways (lower total indicates higher priority).</li>
 * </ol>
 * <p>
 * This heuristic helps solvers decide which states to keep and which to discard during
 * relaxed or bounded search, promoting states closer to completion and with lower cumulative
 * runway occupancy times.
 * </p>
 *
 * @see ALPState
 * @see StateRanking
 */
public class ALPRanking implements StateRanking<ALPState> {
    /**
     * Compares two ALP states according to the number of remaining aircraft
     * and the sum of runway previous landing times.
     *
     * @param a the first state to compare
     * @param b the second state to compare
     * @return a negative integer, zero, or a positive integer if {@code a} is ranked
     *         less than, equal to, or greater than {@code b}, respectively
     */
    @Override
    public int compare(ALPState a, ALPState b) {
        int remAircraftA = Arrays.stream(a.remainingAircraftOfClass).sum();
        int remAircraftB = Arrays.stream(b.remainingAircraftOfClass).sum();
        int totA = Arrays.stream(a.runwayStates).map(i -> i.prevTime).reduce(0, Integer::sum);
        int totB = Arrays.stream(b.runwayStates).map(i -> i.prevTime).reduce(0, Integer::sum);

        int remCompare = Integer.compare(remAircraftA, remAircraftB);
        if (remCompare == 0) return Integer.compare(totA, totB);
        else return remCompare;
    }
}
