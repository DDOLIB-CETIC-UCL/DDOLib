package org.ddolib.examples.maximumcoverage;

import org.ddolib.ddo.core.heuristics.cluster.StateDistance;
import org.ddolib.ddo.core.mdd.NodeSubProblem;
import static org.ddolib.util.DistanceUtil.weightedJaccardDistance;
import static org.ddolib.util.DistanceUtil.symmetricDifferenceDistance;

import java.util.BitSet;

import static java.lang.Math.abs;
import static java.lang.Math.max;

public class MaxCoverDistance implements StateDistance<MaxCoverState> {
    MaxCoverProblem instance;

    public MaxCoverDistance(MaxCoverProblem instance) {
            this.instance = instance;
        }

    private double rogerDistance(BitSet a, BitSet b) {
        BitSet tmp = (BitSet) a.clone();
        tmp.and(b);
        int intersectionSize = tmp.cardinality();
        return 50*50 - intersectionSize*intersectionSize;
    }

    private double convexCombination(double distanceOnSet, double distanceOnCost) {
        double alpha = 0.25;
        return alpha * distanceOnCost + (1 - alpha) * distanceOnSet;
    }

    @Override
    public double distanceWithRoot(MaxCoverState state) {
            return ((double) state.coveredItems().cardinality()) /instance.nbItems;
    }

    @Override
    public double distance(NodeSubProblem<MaxCoverState> a, NodeSubProblem<MaxCoverState> b) {
        double distanceOnSet = weightedJaccardDistance(a.state.coveredItems(), b.state.coveredItems(), instance.centralities);
        double distanceOnCost = abs(a.getValue() - b.getValue()) / instance.nbItems;
        return convexCombination(distanceOnSet, distanceOnCost);
    }

    @Override
    public double distance(MaxCoverState a, MaxCoverState b) {
        return symmetricDifferenceDistance(a.coveredItems(), b.coveredItems()) / instance.nbItems;
    }


}
