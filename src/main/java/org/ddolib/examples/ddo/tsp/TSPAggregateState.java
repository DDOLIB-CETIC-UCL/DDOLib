package org.ddolib.examples.ddo.tsp;

public class TSPAggregateState {
    public final TSPState state;
    public final int[] nToVisit; // Number of times each node must be visited

    public TSPAggregateState(TSPState state, int[] nToVisit) {
        this.state = state;
        this.nToVisit = nToVisit;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof TSPAggregateState oState) {
            return state.equals(oState.state);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return state.hashCode();
    }

    @Override
    public String toString() {
        return state.toString();
    }
}
