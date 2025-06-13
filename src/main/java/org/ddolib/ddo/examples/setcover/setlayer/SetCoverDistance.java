package org.ddolib.ddo.examples.setcover.setlayer;

import org.ddolib.ddo.heuristics.StateDistance;

public class SetCoverDistance implements StateDistance<SetCoverState> {

    /**
     * The distance between two states in the set cover problem is the
     * size of the symmetric difference between the two sets of uncovered elements
     * @param a the first state
     * @param b the second state
     * @return
     */
    @Override
    public double distance(SetCoverState a, SetCoverState b) {
        int intersectionSize = 0;
        for (int elem: a.uncoveredElements.keySet()) {
            if(b.uncoveredElements.containsKey(elem)) {
                intersectionSize++;
            }
        }
        return a.uncoveredElements.size() + b.uncoveredElements.size() - 2 * intersectionSize;
    }
}
