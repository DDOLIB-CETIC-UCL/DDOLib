package org.ddolib.examples.alp;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.Relaxation;

import java.util.Arrays;
import java.util.Iterator;

public class ALPRelax implements Relaxation<ALPState> {

    ALPProblem problem;

    public ALPRelax(ALPProblem problem) {
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
    public double relaxEdge(ALPState from, ALPState to, ALPState merged, Decision d, double cost) {
        return cost;
    }
}
