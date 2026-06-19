package org.ddolib.examples.mispnolayer;

import java.util.BitSet;

/**
 * State for the NoLayer formulation of the Maximum Independent Set Problem.
 *
 * @param remainingNodes The set of nodes that can still be added to the independent set.
 * @param lastSelected   The index of the last node added to the independent set, used for symmetry breaking.
 */
public record MispNoLayerState(BitSet remainingNodes, int lastSelected) {
    public MispNoLayerState {
        remainingNodes = (BitSet) remainingNodes.clone();
    }

    @Override
    public String toString() {
        return String.format("MispNoLayerState(lastSelected=%d, remainingNodes=%s)", lastSelected, remainingNodes);
    }
}
