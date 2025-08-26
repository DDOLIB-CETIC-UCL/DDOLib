package org.ddolib.examples.ddo.misp;



import org.ddolib.ddo.core.heuristics.cluster.StateDistance;

import java.util.BitSet;
import static java.lang.Math.max;

public class MispDistance implements StateDistance<BitSet> {
    @Override
    public double distance(BitSet a, BitSet b) {
        double symmetricDifference = 0.0;

        for (int i = 0; i < max(a.length(), b.length()); i++) {
            if (a.get(i) != b.get(i)) {
                symmetricDifference++;
            }
        }

        return symmetricDifference;
    }
}
