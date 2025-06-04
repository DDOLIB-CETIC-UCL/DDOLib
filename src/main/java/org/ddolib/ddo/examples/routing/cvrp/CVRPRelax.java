package org.ddolib.ddo.examples.routing.cvrp;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Relaxation;

import java.util.Iterator;

public class CVRPRelax implements Relaxation<CVRPState> {
    @Override
    public CVRPState mergeStates(Iterator<CVRPState> states) {
        return null;
    }

    @Override
    public int relaxEdge(CVRPState from, CVRPState to, CVRPState merged, Decision d, int cost) {
        return 0;
    }
}
