package org.ddolib.examples.salbp1;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.Relaxation;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;

public class SALBPRelax implements Relaxation<SALBPState> {

    final SALBPProblem problem;
    public SALBPRelax(SALBPProblem problem) {
        this.problem = problem;
    }

    @Override
    public SALBPState mergeStates(final Iterator<SALBPState> states) {
        SALBPState current = states.next();
        BitSet intersectionStations = (BitSet) current.stations().clone();
        BitSet unionCurrentStation = (BitSet) current.currentStation().clone();
        BitSet unionStations = (BitSet) current.stations().clone();
        while (states.hasNext()) {
            SALBPState state = states.next();
            intersectionStations.and(state.stations());
            unionCurrentStation.or(state.currentStation());
            unionStations.or(state.stations());
        }
        unionStations.andNot(intersectionStations);
        unionCurrentStation.or(unionStations);
        double remainingDuration = problem.cycleTime;
        for (int i = unionCurrentStation.nextSetBit(0); i >= 0; i = unionCurrentStation.nextSetBit(i + 1)) {
            remainingDuration -= problem.durations[i];
        }
        if (remainingDuration <= 0) {
            intersectionStations.or(unionCurrentStation);
            return new SALBPState(intersectionStations, new BitSet(problem.nbTasks), problem.cycleTime);
        } else {
            return new SALBPState(intersectionStations, unionCurrentStation, remainingDuration);
        }
    }

    @Override
    public double relaxEdge(SALBPState from, SALBPState to, SALBPState merged, Decision d, double cost) {
        if (merged.currentStation().isEmpty())
            return cost + 1;
        else return cost;
    }
}
