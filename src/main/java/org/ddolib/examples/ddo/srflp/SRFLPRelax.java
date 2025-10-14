package org.ddolib.examples.ddo.srflp;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.Relaxation;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Iterator;

import static java.lang.Integer.max;
import static java.lang.Integer.min;

/**
 * Implementation of the relaxation for the SRFLP.
 */
public class SRFLPRelax implements Relaxation<SRFLPState> {

    private final SRFLPProblem problem;

    /**
     * Constructs a new instance of a relaxation for the SRFLP.
     *
     * @param problem The problem instance that must be solved.
     */
    public SRFLPRelax(SRFLPProblem problem) {
        this.problem = problem;

    }


    @Override
    public SRFLPState mergeStates(Iterator<SRFLPState> states) {
        BitSet mergedMust = new BitSet(problem.nbVars());
        mergedMust.set(0, problem.nbVars(), true);
        BitSet mergedMaybes = new BitSet(problem.nbVars());
        int[] mergedCut = new int[problem.nbVars()];
        Arrays.fill(mergedCut, Integer.MAX_VALUE);
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

        mergedMaybes.andNot(mergedMust);

        return new SRFLPState(mergedMust, mergedMaybes, mergedCut, mergedDepth);
    }

    @Override
    public double relaxEdge(SRFLPState from, SRFLPState to, SRFLPState merged, Decision d,
                            double cost) {
        return cost;
    }
}
