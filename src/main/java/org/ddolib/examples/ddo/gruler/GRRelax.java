package org.ddolib.examples.ddo.gruler;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.Relaxation;

import java.util.BitSet;
import java.util.Iterator;

public class GRRelax implements Relaxation<GRState> {
    @Override
    public GRState mergeStates(final Iterator<GRState> states) {
        // take the intersection of the marks and distances sets
        GRState curr = states.next();
        BitSet intersectionMarks = (BitSet) curr.getMarks().clone();
        BitSet intersectionDistances = (BitSet) curr.getDistances().clone();
        int lastMark = curr.getLastMark();
        while (states.hasNext()) {
            GRState state = states.next();
            intersectionMarks.and(state.getMarks());
            intersectionDistances.and(state.getDistances());
            lastMark = Math.min(lastMark, state.getLastMark());
        }
        return new GRState(intersectionMarks, intersectionDistances, lastMark);
    }

    @Override
    public double relaxEdge(GRState from, GRState to, GRState merged, Decision d, double cost) {
        return cost;
    }
}
