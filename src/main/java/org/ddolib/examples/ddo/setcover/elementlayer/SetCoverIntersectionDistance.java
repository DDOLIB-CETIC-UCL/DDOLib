package org.ddolib.examples.ddo.setcover.elementlayer;

import org.ddolib.ddo.heuristics.StateDistance;

public class SetCoverIntersectionDistance implements StateDistance<SetCoverState> {

    /**
     * The distance between two states in the set cover problem is the
     * size of the intersection the two sets of uncovered elements
     * @param a the first state
     * @param b the second state
     * @return
     */
    @Override
    public double distance(SetCoverState a, SetCoverState b) {
        SetCoverState smaller = a.uncoveredElements.size() < b.uncoveredElements.size() ? a : b;
        SetCoverState larger = a.uncoveredElements.size() < b.uncoveredElements.size() ? b : a;
        int intersectionSize = 0;
        for (int elem: smaller.uncoveredElements) {
            if(larger.uncoveredElements.contains(elem)) {
                intersectionSize++;
            }
        }
        return -intersectionSize;
    }
}
