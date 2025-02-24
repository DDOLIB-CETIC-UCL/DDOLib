package org.ddolib.ddo.examples.TSPTW;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Relaxation;

import java.util.Iterator;

public class TSPTWRelax implements Relaxation<TSPTWState> {
    @Override
    public TSPTWState mergeStates(Iterator<TSPTWState> states) {
        return null;
    }

    @Override
    public int relaxEdge(TSPTWState from, TSPTWState to, TSPTWState merged, Decision d, int cost) {
        return 0;
    }
}
