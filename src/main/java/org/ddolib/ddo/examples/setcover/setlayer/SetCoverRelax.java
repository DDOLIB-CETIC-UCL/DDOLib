package org.ddolib.ddo.examples.setcover.setlayer;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Relaxation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SetCoverRelax implements Relaxation<SetCoverState> {

    @Override
    public SetCoverState mergeStates(Iterator<SetCoverState> states) {
        SetCoverState currState = states.next();
        SetCoverState newState = currState.clone();
        while (states.hasNext()) {
            currState = states.next();
            newState.uncoveredElements.keySet().retainAll(currState.uncoveredElements.keySet());
        }
        return newState;
    }

    @Override
    public int relaxEdge(SetCoverState from, SetCoverState to, SetCoverState merged, Decision d, int cost) {
        return cost;
    }

}