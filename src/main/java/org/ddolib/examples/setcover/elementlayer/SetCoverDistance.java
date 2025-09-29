package org.ddolib.examples.setcover.elementlayer;

import org.ddolib.ddo.core.heuristics.cluster.StateDistance;

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
        SetCoverState smaller = a.uncoveredElements.size() < b.uncoveredElements.size() ? a : b;
        SetCoverState larger = a.uncoveredElements.size() < b.uncoveredElements.size() ? b : a;
        for (int elem: smaller.uncoveredElements) {
            if(larger.uncoveredElements.contains(elem)) {
                intersectionSize++;
            }
        }
        return a.uncoveredElements.size() + b.uncoveredElements.size() - 2 * intersectionSize;
    }

    @Override
    public double distanceWithBase(SetCoverState a) {
        return a.uncoveredElements.size();
    }
}
