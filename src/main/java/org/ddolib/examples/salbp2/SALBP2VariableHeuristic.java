package org.ddolib.examples.salbp2;

import org.ddolib.ddo.core.heuristics.variable.VariableHeuristic;

import java.util.BitSet;
import java.util.Iterator;
import java.util.Set;

public class SALBP2VariableHeuristic implements VariableHeuristic<SALBP2State> {

    @Override
    public Integer nextVariable(final Set<Integer> variables, final Iterator<SALBP2State> states) {
        SALBP2State state = states.next();
        int m = state.stations().length;
        BitSet[] stations = new BitSet[m];
        for (int i = 0; i < m; i++) {
            stations[i] = (BitSet) state.stations()[i].clone();
        }
        while (states.hasNext()) {
            state = states.next();
            for (int i = 0; i < m; i++) {
                stations[i].or(state.stations()[i]);
            }
        }




        return variables.iterator().next();
    }
}
