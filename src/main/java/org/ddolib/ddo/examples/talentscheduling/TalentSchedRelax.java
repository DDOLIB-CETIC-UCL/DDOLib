package org.ddolib.ddo.examples.talentscheduling;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Relaxation;

import java.util.BitSet;
import java.util.Iterator;

public class TalentSchedRelax implements Relaxation<TalentSchedState> {

    private final int nbVar;

    public TalentSchedRelax(int nbVar) {
        this.nbVar = nbVar;
    }

    @Override
    public TalentSchedState mergeStates(Iterator<TalentSchedState> states) {
        BitSet mergedRemaining = new BitSet(nbVar);
        mergedRemaining.set(0, nbVar);
        BitSet mergedMaybe = new BitSet(nbVar);

        while (states.hasNext()) {
            TalentSchedState state = states.next();
            mergedRemaining.and(state.remainingScenes());
            mergedMaybe.or(state.remainingScenes());
            mergedMaybe.or(state.maybeScenes());
        }
        mergedMaybe.andNot(mergedRemaining);

        return new TalentSchedState(mergedRemaining, mergedMaybe);
    }

    @Override
    public int relaxEdge(TalentSchedState from, TalentSchedState to, TalentSchedState merged, Decision d, int cost) {
        return cost;
    }
}
