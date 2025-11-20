package org.ddolib.examples.maximumcoverage;

import org.ddolib.ddo.core.heuristics.cluster.StateDistance;

import java.util.BitSet;

import static java.lang.Math.max;

public class MaxCoverWeightedJaccardDistance implements StateDistance<MaxCoverState> {
    final private MaxCoverProblem instance;

    public MaxCoverWeightedJaccardDistance(MaxCoverProblem instance) {
        this.instance = instance;
    }

    private double weightedJaccardDistance(BitSet a, BitSet b) {
        double intersectionSize =0;
        double unionSize = 0;

        int maxIndex = max(a.length(), b.length());
        for (int i = 0; i < maxIndex; i++) {
            if (a.get(i) || b.get(i)) {
                unionSize += instance.centralities[i];
                if (a.get(i) && b.get(i)) {
                    intersectionSize+= instance.centralities[i];
                }
            }
        }

        return 1 - intersectionSize / unionSize;
    }


    @Override
    public double distance(MaxCoverState a, MaxCoverState b) {
        return weightedJaccardDistance(a.coveredItems(), b.coveredItems());
    }
}
