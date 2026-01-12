package org.ddolib.examples.misp;

import org.ddolib.ddo.core.heuristics.cluster.StateDistance;
import static org.ddolib.util.DistanceUtil.symmetricDifferenceDistance;

import java.util.BitSet;

public class MispDistance implements StateDistance<BitSet> {

    @Override
    public double distance(BitSet a, BitSet b) {
        return symmetricDifferenceDistance(a, b);
    }

}
