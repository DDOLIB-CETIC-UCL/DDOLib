package org.ddolib.examples.alp;

import org.ddolib.modeling.StateRanking;

import java.util.Arrays;

/**
 * Ranking for {@link org.ddolib.examples.alp.ALPState}.
 * <p>
 * First compares remaining aircraft to land, then the sum of previous time of the runways.
 * </P>
 */
public class ALPRanking implements StateRanking<ALPState> {

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
