package org.ddolib.examples.setcover.elementlayer;

import org.ddolib.ddo.core.heuristics.cluster.StateDistance;
import org.ddolib.examples.LaunchInterface;

import java.util.HashSet;
import java.util.Set;

import static org.ddolib.examples.LaunchInterface.DistanceType;

public class SetCoverDistance implements StateDistance<SetCoverState> {

    private final SetCoverProblem instance;
    private final DistanceType distanceType;

    public SetCoverDistance(SetCoverProblem instance, DistanceType distanceType) {
        this.instance = instance;
        this.distanceType = distanceType;
    }

    public SetCoverDistance(SetCoverProblem instance) {
        this(instance, DistanceType.SYM);
    }

    private double symmetricDifference(Set<Integer> a, Set<Integer> b) {
        int intersectionSize = 0;
        Set<Integer> smaller = a.size() < b.size() ? a : b;
        Set<Integer> larger = a.size() < b.size() ? b : a;
        for (int elem: smaller) {
            if(larger.contains(elem)) {
                intersectionSize++;
            }
        }
        return a.size() + b.size() - 2 * intersectionSize;
    }

    private double jaccardDistance(Set<Integer> a, Set<Integer> b) {
        double intersectionSize = 0;
        double unionSize = 0;
        for (int elem = 0; elem < instance.nElem; elem++) {
            if (a.contains(elem) || b.contains(elem)) {
                unionSize++;
                if (a.contains(elem) && b.contains(elem)) {
                    intersectionSize++;
                }
            }
        }

        return 1 - (intersectionSize / unionSize);
    }

    private double weightedSymmetricDifference(Set<Integer> a, Set<Integer> b) {
        double distance = 0.0;

        for (int i = 0; i < instance.nElem; i++) {
            if (a.contains(i) != b.contains(i)) {
                distance += instance.elemMinWeights.get(i);
            }
        }

        return distance;
    }

    private double weightedJaccardDistance(Set<Integer> a, Set<Integer> b) {
        double intersectionSize = 0;
        double unionSize = 0;
        for (int elem = 0; elem < instance.nElem; elem++) {
            if (a.contains(elem) || b.contains(elem)) {
                unionSize += instance.elemMinWeights.get(elem);
                if (a.contains(elem) && b.contains(elem)) {
                    intersectionSize += instance.elemMinWeights.get(elem);
                }
            }
        }

        return 1 - (intersectionSize / unionSize);
    }

    private double diceDistance(Set<Integer> a, Set<Integer> b) {
        double intersectionSize = 0;
        Set<Integer> smaller = a.size() < b.size() ? a : b;
        Set<Integer> larger = a.size() < b.size() ? b : a;
        for (int elem: smaller) {
            if(larger.contains(elem)) {
                intersectionSize++;
            }
        }

        return 1 - 2*intersectionSize/(a.size() + b.size());
    }


    /**
     * The distance between two states in the set cover problem is the
     * size of the symmetric difference between the two sets of uncovered elements
     * @param a the first state
     * @param b the second state
     * @return
     */
    @Override
    public double distance(SetCoverState a, SetCoverState b) {
        double distance = 0.0;
        switch (this.distanceType) {
            case JAC -> distance = jaccardDistance(a.uncoveredElements, b.uncoveredElements);
            case SYM -> distance = symmetricDifference(a.uncoveredElements, b.uncoveredElements);
            case DICE -> distance = diceDistance(a.uncoveredElements, b.uncoveredElements);
            case WJAC -> distance = weightedJaccardDistance(a.uncoveredElements, b.uncoveredElements);
            case WSYM -> distance = weightedSymmetricDifference(a.uncoveredElements, b.uncoveredElements);
        }

        return distance;
        // return symmetricDifference(a.uncoveredElements, b.uncoveredElements);
    }


}
