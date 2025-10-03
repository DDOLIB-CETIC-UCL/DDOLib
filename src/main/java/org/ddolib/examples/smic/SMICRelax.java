package org.ddolib.examples.smic;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.Relaxation;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class SMICRelax implements Relaxation<SMICState> {
    final SMICProblem problem;

    public SMICRelax(SMICProblem problem) {
        this.problem = problem;
    }
    @Override
    public SMICState mergeStates(final Iterator<SMICState> states) {

        SMICState state = states.next();
        Set<Integer> intersectionJobs = new HashSet<>(state.remainingJobs());
        int minCurrentTime = state.currentTime();
        int minCurrentInventory = state.minCurrentInventory();
        int maxCurrentInventory = state.maxCurrentInventory();

        while (states.hasNext()) {
            state = states.next();
            intersectionJobs.retainAll(state.remainingJobs());
            minCurrentTime = Math.min(minCurrentTime, state.currentTime());
            minCurrentInventory = Math.max(minCurrentInventory, state.minCurrentInventory());
            maxCurrentInventory = Math.min(maxCurrentInventory, state.maxCurrentInventory());
        }
        return new SMICState(intersectionJobs, minCurrentTime, minCurrentInventory, maxCurrentInventory);
    }

    @Override
    public double relaxEdge(SMICState from, SMICState to, SMICState merged, Decision d, double cost) {
        return cost;
    }
}

