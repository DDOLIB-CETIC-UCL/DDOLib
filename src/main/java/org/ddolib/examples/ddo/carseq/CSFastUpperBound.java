package org.ddolib.examples.ddo.carseq;

import org.ddolib.modeling.FastUpperBound;

import java.util.Set;

public class CSFastUpperBound implements FastUpperBound<CSState> {
    @Override
    public double fastUpperBound(CSState state, Set<Integer> variables) {
        return -state.lowerBound;
    }
}
