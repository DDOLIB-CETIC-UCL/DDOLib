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
//        final SMICState s = states.next();
//        Set<Integer> intersectionJobs = new HashSet<>(s.getRemainingJobs());
        Set<Integer> intersectionJobs = new HashSet<>();
        int minCurrentTime = Integer.MAX_VALUE;
        int minCurrentInventory = Integer.MIN_VALUE;
        int maxCurrentInventory = Integer.MAX_VALUE;
        while (states.hasNext()) {
            final SMICState state = states.next();
            intersectionJobs.addAll(state.getRemainingJobs());
            minCurrentTime = Math.min(minCurrentTime, state.getCurrentTime());
            minCurrentInventory = Math.max(minCurrentInventory, state.getMinCurrentInventory());
            maxCurrentInventory = Math.min(maxCurrentInventory, state.getMaxCurrentInventory());
        }
        return new SMICState(intersectionJobs, minCurrentTime, minCurrentInventory, maxCurrentInventory);
    }
    @Override
    public double relaxEdge(SMICState from, SMICState to, SMICState merged, Decision d, double cost) {
        return cost;
    }
}

