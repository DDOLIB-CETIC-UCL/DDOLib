package org.ddolib.examples.maximumcoverage;

import org.ddolib.ddo.core.heuristics.cluster.StateDistance;

import java.util.BitSet;

import static java.lang.Math.max;

public class MaxCoverDistance implements StateDistance<MaxCoverState> {

    private double jaccardDistance(BitSet a, BitSet b) {

        BitSet tmp = (BitSet) a.clone();
        tmp.and(b);
        int intersectionSize = tmp.cardinality();

        tmp = (BitSet) a.clone();
        tmp.or(b);
        int unionSize = tmp.cardinality();

        return (1.0 - ((double) intersectionSize) / unionSize);
    }

    private double diceDistance(BitSet a, BitSet b) {
        double distance = 0;

        int maxIndex = max(a.length(), b.length());
        for (int i = 0; i < maxIndex; i++) {
            if (a.get(i) && b.get(i)) {
                distance++;
            }
        }
        distance = distance*-2;
        distance = distance / (a.cardinality() + b.cardinality());
        distance += 1;

        return distance;
    }

    private double hammingDistance(BitSet a, BitSet b) {
        double distance = 0;
        int maxIndex = max(a.length(), b.length());
        for (int i = 0; i < maxIndex; i++) {
            if (a.get(i) != b.get(i)) {
                distance++;
            }
        }

        return distance;
    }

    @Override
    public double distance(MaxCoverState a, MaxCoverState b) {
        return jaccardDistance(a.coveredItems(), b.coveredItems());
    }
}
