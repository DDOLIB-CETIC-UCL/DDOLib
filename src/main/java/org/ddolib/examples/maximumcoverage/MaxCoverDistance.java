package org.ddolib.examples.maximumcoverage;

import org.ddolib.ddo.core.heuristics.cluster.StateDistance;
import org.ddolib.ddo.core.mdd.NodeSubProblem;
import org.ddolib.examples.tsalt.TSState;

import java.util.BitSet;

import static java.lang.Math.abs;
import static java.lang.Math.max;

public class MaxCoverDistance implements StateDistance<MaxCoverState> {
        MaxCoverProblem instance;

        public MaxCoverDistance(MaxCoverProblem instance) {
            this.instance = instance;
        }

        private double jaccardDistance(BitSet a, BitSet b) {

            BitSet tmp = (BitSet) a.clone();
            tmp.and(b);
            int intersectionSize = tmp.cardinality();

            tmp = (BitSet) a.clone();
            tmp.or(b);
            int unionSize = tmp.cardinality();

            return (1.0 - ((double) intersectionSize) / unionSize);
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

    private double rogerDistance(BitSet a, BitSet b) {
        BitSet tmp = (BitSet) a.clone();
        tmp.and(b);
        int intersectionSize = tmp.cardinality();
        return 50*50 - intersectionSize*intersectionSize;
    }

    private double symmetricDifferenceDistance(BitSet a, BitSet b) {
        BitSet tmp = (BitSet) a.clone();
        tmp.xor(b);
        return ((double) tmp.cardinality()) / instance.nbItems;
    }

    private double convexCombination(double distanceOnSet, double distanceOnCost) {
        double alpha = 0.25;
        return alpha * distanceOnCost + (1 - alpha) * distanceOnSet;
    }

    private double euclideanDistance(BitSet a, BitSet b) {
        BitSet symmetricDifference = (BitSet) a.clone();
        symmetricDifference.xor(b);
        double dist = symmetricDifference.cardinality();

        return Math.sqrt(dist) / Math.sqrt(instance.nbItems);
    }

    @Override
    public double distanceWithRoot(MaxCoverState state) {
            return ((double) state.coveredItems().cardinality()) /instance.nbItems;
    }

    @Override
    public double distance(NodeSubProblem<MaxCoverState> a, NodeSubProblem<MaxCoverState> b) {
        // double distanceOnSet = jaccardDistance(a.state.coveredItems(),b.state.coveredItems());
        double distanceOnSet = weightedJaccardDistance(a.state.coveredItems(), b.state.coveredItems());
        double distanceOnCost = abs(a.getValue() - b.getValue()) / instance.nbItems; // TODO normalize this distance
        // System.out.println(distanceOnCost);
        return convexCombination(distanceOnSet, distanceOnCost);
    }

    @Override
    public double distance(MaxCoverState a, MaxCoverState b) {
            // return euclideanDistance(a.coveredItems(), b.coveredItems());
        // return weightedJaccardDistance(a.coveredItems(), b.coveredItems());
        return symmetricDifferenceDistance(a.coveredItems(), b.coveredItems());
        // return jaccardDistance(a.coveredItems(), b.coveredItems());
        //return diceDistance(a.coveredItems(), b.coveredItems());
        //return rogerDistance(a.coveredItems(),b.coveredItems());
    }


}
