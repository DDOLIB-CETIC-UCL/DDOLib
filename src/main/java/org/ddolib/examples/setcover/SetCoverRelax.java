package org.ddolib.examples.setcover;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.Relaxation;

import java.util.BitSet;
import java.util.Iterator;

public class SetCoverRelax implements Relaxation<SetCoverState> {

    /**
     * Merge SetCoverStates. Here to merge states we compute the intersection of their uncoveredElements.
     * @param states the set of states that must be merged.
     * @return a new merged SetCoverState.
     */
    @Override
    public SetCoverState mergeStates(Iterator<SetCoverState> states) {
        SetCoverState currState = states.next();
        BitSet intersectionUncoveredItems = (BitSet) currState.uncoveredItems().clone();
        while (states.hasNext()) {
            currState = states.next();
            intersectionUncoveredItems.and(currState.uncoveredItems());
        }
        return new SetCoverState(intersectionUncoveredItems);
    }

    @Override
    public double relaxEdge(SetCoverState from, SetCoverState to, SetCoverState merged, Decision d, double cost) {
        return cost;
    }

}