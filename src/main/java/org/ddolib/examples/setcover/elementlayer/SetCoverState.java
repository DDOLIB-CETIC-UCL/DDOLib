package org.ddolib.examples.setcover.elementlayer;

import java.util.HashSet;
import java.util.Set;

public class SetCoverState {
    public Set<Integer> uncoveredElements; // the element in the universe that are not yet covered

    public SetCoverState(Set<Integer> uncoveredElements) {
        this.uncoveredElements = uncoveredElements;
    }

    @Override
    public SetCoverState clone() {
        return new SetCoverState(new HashSet<>(uncoveredElements));
    }

    @Override
    public String toString() {
        return uncoveredElements.toString();
    }

    @Override
    public int hashCode() {
        return uncoveredElements.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        assert o instanceof SetCoverState;
        return uncoveredElements.equals(((SetCoverState) o).uncoveredElements);
    }
}
