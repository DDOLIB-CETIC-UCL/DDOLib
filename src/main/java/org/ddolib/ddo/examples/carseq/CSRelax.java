package org.ddolib.ddo.examples.carseq;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Relaxation;

import java.util.Iterator;
import java.util.Set;

public class CSRelax implements Relaxation<CSState> {
    @Override
    public CSState mergeStates(Iterator<CSState> states) {
        return null;
    }

    @Override
    public double relaxEdge(CSState from, CSState to, CSState merged, Decision d, double cost) {
        return cost;
    }

    @Override
    public double fastUpperBound(CSState state, Set<Integer> variables) {
        return 0;
    }
}
