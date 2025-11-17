package org.ddolib.examples.salbp2;

import org.ddolib.ddo.core.heuristics.variable.VariableHeuristic;
import org.ddolib.modeling.Problem;

import java.util.BitSet;
import java.util.Iterator;
import java.util.Set;

public class SALBP2VariableHeuristic implements VariableHeuristic<SALBP2State> {

    @Override
    public Integer nextVariable(final Set<Integer> variables, final Iterator<SALBP2State> states) {
        return variables.iterator().next();
    }
}
