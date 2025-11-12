package org.ddolib.examples.salbp1;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.Relaxation;
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
        BitSet intersectionRemainingTasks = (BitSet) current.remainingTasks().clone();
        BitSet unionCurrentStation = (BitSet) current.currentStation().clone();
        while (states.hasNext()) {
            SALBPState state = states.next();
            intersectionRemainingTasks.and(state.remainingTasks());
            unionCurrentStation.or(state.currentStation());
        }
        double remainingDuration = problem.cycleTime;
        for (int i = unionCurrentStation.nextSetBit(0); i >= 0; i = unionCurrentStation.nextSetBit(i + 1)) {
            remainingDuration -= problem.durations[i];
        }
        if (remainingDuration <= 0) {
            return new SALBPState(intersectionRemainingTasks, new BitSet(problem.nbTasks), problem.cycleTime);
        } else {
            return new SALBPState(intersectionRemainingTasks, unionCurrentStation, remainingDuration);
        }
    }

    @Override
    public double relaxEdge(SALBPState from, SALBPState to, SALBPState merged, Decision d, double cost) {
//        if (merged.currentStation().isEmpty())
//            return cost + 1;
//        else return cost;
        return  cost;
    }
}
