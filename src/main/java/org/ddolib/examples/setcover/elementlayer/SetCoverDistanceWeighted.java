package org.ddolib.examples.setcover.elementlayer;

import org.ddolib.ddo.core.heuristics.cluster.StateDistance;

import java.util.HashSet;
import java.util.Set;

public class SetCoverDistanceWeighted implements StateDistance<SetCoverState> {

    private final SetCoverProblem instance;

    public SetCoverDistanceWeighted(SetCoverProblem instance) {
        this.instance = instance;
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

    private double symmetricDifference(Set<Integer> a, Set<Integer> b) {
        double distance = 0.0;

        for (int i = 0; i < instance.nElem; i++) {
            if (a.contains(i) != b.contains(i)) {
                distance ++;
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

    private double jaccardDistance(Set<Integer> a, Set<Integer> b) {
        double intersectionSize = 0;
        double unionSize = 0;
        for (int elem = 0; elem < instance.nElem; elem++) {
            if (a.contains(elem) || b.contains(elem)) {
                unionSize ++;
                if (a.contains(elem) && b.contains(elem)) {
                    intersectionSize++;
                }
            }
        }

        return 1 - (intersectionSize / unionSize);
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
       return symmetricDifference(a.uncoveredElements, b.uncoveredElements);
    }
}
