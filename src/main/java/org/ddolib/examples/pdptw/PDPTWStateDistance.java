package org.ddolib.examples.pdptw;

import org.ddolib.ddo.core.heuristics.cluster.StateDistance;

public class PDPTWStateDistance implements StateDistance<PDPTWState> {

    @Override
    public double distance(PDPTWState a, PDPTWState b) {
        boolean dist1 = a.current.nextSetBit(0) == b.current.nextSetBit(0);
        int dist2 = Math.abs(a.minContent - b.minContent);
        double dist3 = Math.abs(a.minCurrentTime - b.minCurrentTime);

        return (dist1 ? 0.0 : 1000.0) + (dist2) + dist3;
    }

}

