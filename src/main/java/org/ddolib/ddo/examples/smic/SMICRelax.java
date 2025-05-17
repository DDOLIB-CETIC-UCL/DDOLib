package org.ddolib.ddo.examples.smic;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Relaxation;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class SMICRelax implements Relaxation<SMICState> {
    final SMICProblem problem;

    public SMICRelax(SMICProblem problem) {this.problem = problem;}

    @Override
    public SMICState mergeStates(final Iterator<SMICState> states) {
        Set<Integer> unionJobs = new HashSet<>();
        int minCurrentTime = Integer.MAX_VALUE;
        int maxCurrentInventory = Integer.MIN_VALUE;
        while (states.hasNext()) {
            final SMICState state = states.next();
            unionJobs.addAll(state.getRemainingJobs());
            minCurrentTime = Math.min(minCurrentTime, state.getCurrentTime());
            maxCurrentInventory = Math.max(maxCurrentInventory, state.getCurrentInventory());
        }
        return new SMICState(unionJobs, minCurrentTime, maxCurrentInventory);
    }
    @Override
    public int relaxEdge(SMICState from, SMICState to, SMICState merged, Decision d, int cost) {
        return cost;
    }
}

