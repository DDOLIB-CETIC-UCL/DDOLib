package org.ddolib.ddo.examples.setcover;

import java.util.HashMap;
import java.util.Map;

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
    public boolean equals(Object o) {
        assert o instanceof SetCoverState;
        return uncoveredElements.keySet().equals(((SetCoverState) o).uncoveredElements.keySet());
    }

    public int size() {
        return uncoveredElements.size();
    }
}
