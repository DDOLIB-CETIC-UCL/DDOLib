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

//        SMICState state = states.next();
        Set<Integer> unionJobs = new HashSet<>(/*state.remainingJobs()*/);
        int minCurrentTime = Integer.MAX_VALUE;
        int minCurrentInventory = Integer.MIN_VALUE;
        int maxCurrentInventory = Integer.MAX_VALUE;

        while (states.hasNext()) {
            final SMICState state = states.next();
            unionJobs.addAll(state.remainingJobs());
            minCurrentTime = Math.min(minCurrentTime, state.currentTime());
            minCurrentInventory = Math.max(minCurrentInventory, state.minCurrentInventory());
            maxCurrentInventory = Math.min(maxCurrentInventory, state.maxCurrentInventory());
        }
        if (minCurrentInventory <= maxCurrentInventory)
            return new SMICState(unionJobs, minCurrentTime, minCurrentInventory, maxCurrentInventory);
        return new SMICState(unionJobs, minCurrentTime, minCurrentInventory, minCurrentInventory);
    }

    @Override
    public double relaxEdge(SMICState from, SMICState to, SMICState merged, Decision d, double cost) {
        return cost;
    }
}

