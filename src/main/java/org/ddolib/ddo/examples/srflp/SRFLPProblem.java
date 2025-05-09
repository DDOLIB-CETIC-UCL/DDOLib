package org.ddolib.ddo.examples.srflp;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Problem;

import java.util.BitSet;
import java.util.Iterator;

public class SRFLPProblem implements Problem<SRFLPState> {

    public final int[] lengths;
    public final int[][] flows;

    public SRFLPProblem(int[] lengths, int[][] flows) {
        this.lengths = lengths;
        this.flows = flows;
    }

    @Override
    public int nbVars() {
        return lengths.length;
    }

    @Override
    public SRFLPState initialState() {
        BitSet all = new BitSet(nbVars());
        all.set(0, nbVars());
        return new SRFLPState(all, new BitSet(nbVars()), new int[nbVars()], 0);
    }

    @Override
    public int initialValue() {
        return 0;
    }

    @Override
    public Iterator<Integer> domain(SRFLPState state, int var) {
        return state.remaining().stream().iterator();
    }

    @Override
    public SRFLPState transition(SRFLPState state, Decision decision) {
        BitSet newRemaining = new BitSet(nbVars());
        newRemaining.or(state.remaining());
        newRemaining.clear(decision.val());

        int[] newCut = new int[nbVars()];
        for (int i = newRemaining.nextSetBit(0); i >= 0; i = newRemaining.nextSetBit(i + 1)) {
            newCut[i] = state.cut()[i] + flows[decision.val()][i];
        }


        return new SRFLPState(newRemaining, state.maybe(), newCut, state.depth() + 1);
    }

    @Override
    public int transitionCost(SRFLPState state, Decision decision) {
        int cut = 0;

        for (int i = state.remaining().nextSetBit(0); i >= 0; i = state.remaining().nextSetBit(i + 1)) {
            if (i != decision.val()) cut += state.cut()[i];
        }

        return -cut * lengths[decision.val()];
    }

    public double rootValue() {
        double toReturn = 0;
        for (int i = 0; i < nbVars(); i++) {
            for (int j = i + 1; j < nbVars(); j++) {
                toReturn += flows[i][j] * (lengths[i] + lengths[j]) * 0.5;
            }
        }
        return toReturn;
    }
}
