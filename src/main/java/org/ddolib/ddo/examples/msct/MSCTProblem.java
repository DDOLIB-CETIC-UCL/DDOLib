package org.ddolib.ddo.examples.msct;


import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Problem;

import java.util.*;
import java.util.stream.IntStream;

class MSCTProblem implements Problem<MSCTState> {

    final int n;
    final int[] release; // release date of each job
    final int[] processing; // processing time of each job

    public MSCTProblem(final int[] release, final int[] processing) {
        this.release = release;
        this.processing = processing;
        this.n = release.length;
    }

    @Override
    public int nbVars() {
        return n;
    }

    @Override
    public MSCTState initialState() {
        Set<Integer> jobs = new HashSet<>();
        for (int i = 0; i < nbVars(); i++) {
            jobs.add(i);
        }
        return new MSCTState(jobs,0);
    }

    @Override
    public int initialValue() {
        return 0;
    }

    @Override
    public Iterator<Integer> domain(MSCTState state, int var) {
        ArrayList<Integer> domain = new ArrayList<>();
        for (Integer job : state.remainingJobs) {
            domain.add(job);
        }
        return domain.iterator();
    }

    @Override
    public MSCTState transition(MSCTState state, Decision decision) {
        Set<Integer> remaining = new HashSet<>(state.remainingJobs);
        remaining.remove(decision.var());
        int currentTime = Math.max(state.getCurrentTime(), release[decision.val()]) + processing[decision.val()];
        return new MSCTState(remaining,currentTime);
    }

    @Override
    public int transitionCost(MSCTState state, Decision decision) {
        int currentTime = Math.max(state.getCurrentTime(), release[decision.val()]) + processing[decision.val()];
        return -currentTime;
    }
}
