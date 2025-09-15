package org.ddolib.examples.ddo.alp;

import org.ddolib.modeling.FastUpperBound;

import java.util.HashSet;
import java.util.Set;

public class ALPFastUpperBound implements FastUpperBound<ALPState> {

    private final ALPProblem problem;

    public ALPFastUpperBound(ALPProblem problem) {
        this.problem = problem;
    }

    @Override
    public double fastUpperBound(ALPState state, Set<Integer> variables) {
        int sum = 0;
        ALPInstance inst = problem.instance;

        for (int c = 0; c < problem.instance.nbClasses; c++) {
            for (int r = 0; r < state.remainingAircraftOfClass[c]; r++) {
                //rem(c) always start with a 0
                int aircraft = problem.latestToEarliestAircraftByClass.get(c).get(r + 1);
                int bestRunwayTardiness = Integer.MAX_VALUE;
                for (int runway = 0; runway < state.runwayStates.length; runway++) {
                    int arrivalTime = problem.getArrivalTime(state.runwayStates, aircraft, runway);
                    if (arrivalTime - inst.aircraftDeadline[aircraft] <= 0) {
                        // If one aircraft can not land, the fub is maxValue
                        int estimatedTardiness = Math.max(0, arrivalTime - inst.aircraftTarget[aircraft]);
                        bestRunwayTardiness = Math.min(bestRunwayTardiness, estimatedTardiness);
                    }
                }
                if (bestRunwayTardiness == Integer.MAX_VALUE) return (bestRunwayTardiness);
                sum += bestRunwayTardiness;
            }
        }

        return -sum;
    }
}
