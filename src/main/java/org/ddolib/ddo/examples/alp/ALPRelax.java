package org.ddolib.ddo.examples.alp;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Relaxation;

import java.util.*;

public class ALPRelax implements Relaxation<ALPState> {

    ALPProblem problem;

    public ALPRelax(ALPProblem problem){
        this.problem = problem;
    }

    @Override
    public ALPState mergeStates(Iterator<ALPState> states) {
        int[] remainingAircraft = new int[problem.instance.nbClasses];
        Arrays.fill(remainingAircraft, Integer.MAX_VALUE);
        RunwayState[] runwayStates = new RunwayState[problem.instance.nbRunways];
        Arrays.fill(runwayStates, new RunwayState(ALPProblem.DUMMY, Integer.MAX_VALUE));

        // Set the remaining nb of aircraft (for each class) of the merged state as the minimal value of each merged states.
        // Set the previous time of each runway of the merged state as the minimal value of each merged states.
        while (states.hasNext()) {
            ALPState s = states.next();
            for (int i = 0; i < remainingAircraft.length; i++) {
                remainingAircraft[i] = Math.min(remainingAircraft[i], s.remainingAircraftOfClass[i]);
            }
            for (int i = 0; i < runwayStates.length; i++) {
                runwayStates[i].prevTime = Math.min(s.runwayStates[i].prevTime, runwayStates[i].prevTime);
            }
        }

        return new ALPState(remainingAircraft, runwayStates);
    }

    @Override
    public int relaxEdge(ALPState from, ALPState to, ALPState merged, Decision d, int cost) {
        return cost;
    }

    @Override
    public int fastUpperBound(ALPState state, Set<Integer> variables) {
        int sum = 0;
        ALPInstance inst = problem.instance;

        // Basically sum of the current best landing delta of each aircraft.
        // ==> For each aircraft, find the current best runway to land and computes its delta.
        Set<Integer> remainingAircraft = new HashSet<>();
        for(int c = 0; c < problem.instance.nbClasses; c++){
            for(int r = 0; r < state.remainingAircraftOfClass[c]; r++){
                //rem(c) always start with a 0
                int aircraft = problem.latestToEarliestAircraftByClass.get(c).get(r+1);
                int bestRunwayTardiness = Integer.MAX_VALUE;
                for(int runway = 0; runway < state.runwayStates.length; runway++) {
                    int arrivalTime = problem.getArrivalTime(state.runwayStates, aircraft, runway);
                    if(arrivalTime - inst.aircraftDeadline[aircraft] <= 0) {
                        // If one aircraft can not land, the fub is maxValue
                        int estimatedTardiness = Math.max(0, arrivalTime - inst.aircraftTarget[aircraft]);
                        bestRunwayTardiness = Math.min(bestRunwayTardiness,estimatedTardiness);
                    }
                }
                if(bestRunwayTardiness == Integer.MAX_VALUE) return (bestRunwayTardiness);
                sum += bestRunwayTardiness;
            }
        }

        return (-sum);
    }
}
