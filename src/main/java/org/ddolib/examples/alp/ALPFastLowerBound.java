package org.ddolib.examples.alp;

import org.ddolib.modeling.FastLowerBound;

import java.util.Set;

public class ALPFastLowerBound implements FastLowerBound<ALPState> {

    private final ALPProblem problem;

    public ALPFastLowerBound(ALPProblem problem) {
        this.problem = problem;
    }

    @Override
    public double fastLowerBound(ALPState state, Set<Integer> variables) {
        int sum = 0;
        for (int c = 0; c < problem.nbClasses; c++) {
            for (int r = 0; r < state.remainingAircraftOfClass[c]; r++) {
                //rem(c) always start with a 0
                int aircraft = problem.latestToEarliestAircraftByClass.get(c).get(r + 1);
                int bestRunwayTardiness = Integer.MAX_VALUE;
                for (int runway = 0; runway < state.runwayStates.length; runway++) {
                    int arrivalTime = problem.getArrivalTime(state.runwayStates, aircraft, runway);
                    if (arrivalTime - problem.aircraftDeadline[aircraft] <= 0) {
                        // If one aircraft can not land, the fub is maxValue
                        int estimatedTardiness = Math.max(0, arrivalTime - problem.aircraftTarget[aircraft]);
                        bestRunwayTardiness = Math.min(bestRunwayTardiness, estimatedTardiness);
                    }
                }
                if (bestRunwayTardiness == Integer.MAX_VALUE) return (bestRunwayTardiness);
                sum += bestRunwayTardiness;
            }
        }

        return sum;
    }
}
