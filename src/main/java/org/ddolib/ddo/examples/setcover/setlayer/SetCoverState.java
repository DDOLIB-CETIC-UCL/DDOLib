package org.ddolib.ddo.examples.setcover.setlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SetCoverState {
    // Set<Integer> uncoveredElements;
    public Map<Integer, Integer> uncoveredElements;

    public SetCoverState(Map<Integer, Integer> uncoveredElements) {
        this.uncoveredElements = uncoveredElements;
    }

    @Override
    protected SetCoverState clone() {
        return new SetCoverState(new HashMap<>(uncoveredElements));
    }

    @Override
    public String toString() {
        return uncoveredElements.keySet().toString();
    }

    @Override
    public int hashCode() {
        return uncoveredElements.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        assert o instanceof SetCoverState;
        /* System.out.println("***************");
        System.out.println("Equality between: ");
        System.out.println(this);
        System.out.println(o);
        System.out.println(uncoveredElements.keySet().equals(((SetCoverState) o).uncoveredElements.keySet())); */
        return uncoveredElements.keySet().equals(((SetCoverState) o).uncoveredElements.keySet());
    }

    public int size() {
        return uncoveredElements.size();
    }

    /**
     * Compute the size of the intersection between the set of uncovered element in the state and the given set
     * @param set a set of integer
     * @return the number of element that are both present in the given set and the set of uncovered elements in this state
     */
    public int intersectionSize(Set<Integer> set) {
        int count = 0;
        for (Integer i : set) {
            if (uncoveredElements.containsKey(i)) {
                count++;
            }
        }
        return count;

    }
}
