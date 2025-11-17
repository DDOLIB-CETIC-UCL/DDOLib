package org.ddolib.examples.setcover.elementlayer;

import org.ddolib.ddo.core.heuristics.cluster.StateDistance;

import java.util.HashSet;
import java.util.Set;

public class SetCoverDistance implements StateDistance<SetCoverState> {

    private final SetCoverProblem instance;

    public SetCoverDistance(SetCoverProblem instance) {
        this.instance = instance;
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
        return jaccardDistance(a.uncoveredElements, b.uncoveredElements);
        // return symmetricDifference(a.uncoveredElements, b.uncoveredElements);
    }


}
