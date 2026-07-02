package org.ddolib.nolayer.examples.misp;

import java.util.BitSet;

/**
 * State for the NoLayer formulation of the Maximum Independent Set Problem.
 *
 * @param remainingNodes The set of nodes that can still be added to the independent set.
 */
public record MispState(BitSet remainingNodes) {
    public MispState {
        remainingNodes = (BitSet) remainingNodes.clone();
    }

    @Override
    public String toString() {
        return String.format("MispState(remainingNodes=%s)", remainingNodes);
    }
}
