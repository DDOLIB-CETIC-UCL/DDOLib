package org.ddolib.examples.binpacking;

import org.ddolib.ddo.core.heuristics.cluster.StateDistance;
import static org.ddolib.util.DistanceUtil.*;
import static java.lang.Math.abs;

public class BPPDistance implements StateDistance<BPPState> {
    final private BPPProblem instance;

    public BPPDistance(BPPProblem instance) {
        this.instance = instance;
    }

    @Override
    public double distance(BPPState a, BPPState b) {
        return symmetricDifferenceDistance(a.remainingItems, b.remainingItems) +
                abs(a.remainingSpace - b.remainingSpace) +
                abs(a.usedBins - b.usedBins) +
                abs(a.wastedSpace - b.wastedSpace);
    }
}
