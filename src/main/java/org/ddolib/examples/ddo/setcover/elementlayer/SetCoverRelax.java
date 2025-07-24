package org.ddolib.examples.ddo.setcover.elementlayer;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.Relaxation;


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
        SetCoverState newState = currState.clone();
        while (states.hasNext()) {
            currState = states.next();
            newState.uncoveredElements.retainAll(currState.uncoveredElements);
        }
        return newState;
    }

    @Override
    public double relaxEdge(SetCoverState from, SetCoverState to, SetCoverState merged, Decision d, double cost) {
        return cost;
    }

}