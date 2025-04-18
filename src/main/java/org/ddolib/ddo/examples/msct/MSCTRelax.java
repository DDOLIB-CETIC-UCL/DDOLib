package org.ddolib.ddo.examples.msct;


import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Relaxation;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

class MSCTRelax implements Relaxation<MSCTState> {
    final MSCTProblem problem;

    public MSCTRelax(MSCTProblem problem) {
        this.problem = problem;
    }

    @Override
    public MSCTState mergeStates(final Iterator<MSCTState> states) {
        Set<Integer> unionJobs = new HashSet<>();
        int minCurrentTime = Integer.MAX_VALUE;
        while (states.hasNext()) {
            final MSCTState state = states.next();
            unionJobs.addAll(state.getRemainingJobs());
            minCurrentTime = Math.min(state.getCurrentTime(), minCurrentTime);
        }
        return new MSCTState(unionJobs, minCurrentTime);
    }

    @Override
    public int relaxEdge(MSCTState from, MSCTState to, MSCTState merged, Decision d, int cost) {
        return cost;
    }
}
