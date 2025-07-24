package org.ddolib.examples.ddo.setcover.setlayer;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.Relaxation;

import java.util.Iterator;

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
    public double relaxEdge(SetCoverState from, SetCoverState to, SetCoverState merged, Decision d, double cost) {
        return cost;
    }

}