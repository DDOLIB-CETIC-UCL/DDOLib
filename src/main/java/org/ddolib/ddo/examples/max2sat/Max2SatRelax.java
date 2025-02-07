package org.ddolib.ddo.examples.max2sat;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Relaxation;

import java.util.Iterator;

public class Max2SatRelax implements Relaxation<Max2SatState> {
    @Override
    public Max2SatState mergeStates(Iterator<Max2SatState> states) {
        return null;
    }

    @Override
    public int relaxEdge(Max2SatState from, Max2SatState to, Max2SatState merged, Decision d, int cost) {
        return 0;
    }
}
