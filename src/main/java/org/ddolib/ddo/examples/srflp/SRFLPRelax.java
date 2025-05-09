package org.ddolib.ddo.examples.srflp;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Relaxation;

import java.util.BitSet;
import java.util.Iterator;

import static java.lang.Integer.max;
import static java.lang.Integer.min;

public class SRFLPRelax implements Relaxation<SRFLPState> {

    private final SRFLPProblem problem;

    public SRFLPRelax(SRFLPProblem problem) {
        this.problem = problem;
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
        return 0;
    }
}
