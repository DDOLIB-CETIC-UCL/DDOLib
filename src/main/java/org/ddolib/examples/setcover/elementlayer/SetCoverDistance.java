package org.ddolib.examples.setcover.elementlayer;

import org.ddolib.ddo.core.heuristics.cluster.StateDistance;

import java.util.HashSet;
import java.util.Set;

public class SetCoverDistance implements StateDistance<SetCoverState> {

    private final SetCoverProblem instance;

    public SetCoverDistance(SetCoverProblem instance) {
        this.instance = instance;
    }

    /**
     * The distance between two states in the set cover problem is the
     * size of the symmetric difference between the two sets of uncovered elements
     * @param a the first state
     * @param b the second state
     * @return
     */
    /*@Override
    public double distance(SetCoverState a, SetCoverState b) {
        int intersectionSize = 0;
        SetCoverState smaller = a.uncoveredElements.size() < b.uncoveredElements.size() ? a : b;
        SetCoverState larger = a.uncoveredElements.size() < b.uncoveredElements.size() ? b : a;
        for (int elem: smaller.uncoveredElements) {
            if(larger.uncoveredElements.contains(elem)) {
                intersectionSize++;
            }
        }
        return a.uncoveredElements.size() + b.uncoveredElements.size() - 2 * intersectionSize;
    }*/

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
                distance += instance.constraints.get(i).size();
            }
        }

        return distance;
    }


}
