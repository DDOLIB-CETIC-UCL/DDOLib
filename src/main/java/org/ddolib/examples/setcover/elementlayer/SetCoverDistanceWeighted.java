package org.ddolib.examples.setcover.elementlayer;

import org.ddolib.ddo.core.heuristics.cluster.StateDistance;

import java.util.HashSet;
import java.util.Set;

public class SetCoverDistanceWeighted implements StateDistance<SetCoverState> {

    private final SetCoverProblem instance;

    public SetCoverDistanceWeighted(SetCoverProblem instance) {
        this.instance = instance;
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
        // Set<Integer> union = new HashSet<>(a.uncoveredElements);
        // union.addAll(b.uncoveredElements);

        for (int i = 0; i < instance.nElem; i++) {
            if (a.uncoveredElements.contains(i) != b.uncoveredElements.contains(i)) {
                distance += instance.elemMinWeights.get(i);
            }
        }

        return distance;
    }
}
