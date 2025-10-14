package org.ddolib.examples.setcover.setlayer;

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
        Set<Integer> union = new HashSet<>(a.uncoveredElements.keySet());
        union.addAll(b.uncoveredElements.keySet());

        for (Integer i : union) {
            if (!a.uncoveredElements.containsKey(i) || !b.uncoveredElements.containsKey(i)) {
                distance += instance.elemMinWeights.get(i);
            }
        }

        return distance;
    }
}
