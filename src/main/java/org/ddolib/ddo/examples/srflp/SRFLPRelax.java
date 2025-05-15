package org.ddolib.ddo.examples.srflp;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Relaxation;

import java.util.*;

import static java.lang.Integer.max;
import static java.lang.Integer.min;

public class SRFLPRelax implements Relaxation<SRFLPState> {

    private final SRFLPProblem problem;

    private ArrayList<Pair> pairsSortedByFlow = new ArrayList<>();

    public SRFLPRelax(SRFLPProblem problem) {
        this.problem = problem;

        for (int i = 0; i < problem.nbVars(); i++) {
            for (int j = i + 1; j < problem.nbVars(); j++) {
                pairsSortedByFlow.add(new Pair(i, j, problem.flows[i][j]));
            }
        }

        Collections.sort(pairsSortedByFlow);
        Collections.reverse(pairsSortedByFlow);
    }


    @Override
    public SRFLPState mergeStates(Iterator<SRFLPState> states) {
        BitSet mergedMust = new BitSet(problem.nbVars());
        mergedMust.set(0, problem.nbVars());
        BitSet mergedMaybes = new BitSet(problem.nbVars());
        int[] mergedCut = new int[problem.nbVars()];
        int mergedDepth = 0;

        while (states.hasNext()) {
            SRFLPState state = states.next();
            mergedMust.and(state.must());
            mergedMaybes.or(state.must());
            mergedMaybes.or(state.maybe());
            mergedDepth = max(mergedDepth, state.depth());

            for (int i = state.must().nextSetBit(0); i >= 0; i = state.must().nextSetBit(i + 1)) {
                mergedCut[i] = min(mergedCut[i], state.cut()[i]);
            }

            for (int i = state.maybe().nextSetBit(0); i >= 0; i = state.maybe().nextSetBit(i + 1)) {
                mergedCut[i] = min(mergedCut[i], state.cut()[i]);
            }


        }

        return new SRFLPState(mergedMust, mergedMaybes, mergedCut, mergedDepth);
    }

    @Override
    public int relaxEdge(SRFLPState from, SRFLPState to, SRFLPState merged, Decision d, int cost) {
        return cost;
    }

    @Override
    public int fastUpperBound(SRFLPState state, Set<Integer> variables) {
        return Relaxation.super.fastUpperBound(state, variables);
    }

    private static class Pair implements Comparable<Pair> {
        final int x;
        final int y;
        final int flow;


        Pair(int x, int y, int flow) {
            this.x = x;
            this.y = y;
            this.flow = flow;
        }

        @Override
        public int compareTo(Pair o) {
            return Integer.compare(this.flow, o.flow);
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Pair other) return this.x == other.x && this.y == other.y;
            else return false;
        }

        @Override
        public String toString() {
            return String.format("(%d, %d)", x, y);
        }
    }

}
