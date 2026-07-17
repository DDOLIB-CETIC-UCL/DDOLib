package org.ddolib.examples.nolayer.misp;

import org.ddolib.nolayer.modeling.NoLayerDominanceChecker;

import java.util.*;

/**
 * Dominance checker for the Maximum Independent Set Problem (MISP).
 * Assumes a minimization framework where lower values (more negative weights) are better.
 */
public class MispNoLayerDominanceChecker implements NoLayerDominanceChecker<MispState> {

    private final Map<Integer, List<DominanceEntry>> entriesByCardinality = new HashMap<>();

    /**
     * Creates a new instance of this dominance checker.
     */
    public MispNoLayerDominanceChecker() {
    }

    @Override
    public boolean updateDominance(MispState state, double value) {
        BitSet currentNodes = state.remainingNodes();
        int card = currentNodes.cardinality();

        // 1. Check if the new state 'currentNodes' is dominated by any existing entry 's1'
        // An entry can dominate currentNodes only if it has AT LEAST as many remaining nodes
        for (Map.Entry<Integer, List<DominanceEntry>> mapEntry : entriesByCardinality.entrySet()) {
            if (mapEntry.getKey() >= card) {
                for (DominanceEntry entry : mapEntry.getValue()) {
                    // entry.value <= value means the existing entry has a better or equal cost
                    if (entry.value <= value && isSubset(currentNodes, entry.remainingNodes)) {
                        return true; // The new state is dominated, discard it
                    }
                }
            }
        }

        // 2. Remove existing entries that are dominated by the new state 'currentNodes'
        // The new state can dominate an entry only if it has AT MOST as many remaining nodes
        for (Map.Entry<Integer, List<DominanceEntry>> mapEntry : entriesByCardinality.entrySet()) {
            if (mapEntry.getKey() <= card) {
                mapEntry.getValue().removeIf(entry ->
                        value <= entry.value && isSubset(entry.remainingNodes, currentNodes)
                );
            }
        }

        // 3. Add the new state to entries (Safe from self-deletion)
        entriesByCardinality.computeIfAbsent(card, k -> new ArrayList<>()).add(new DominanceEntry(currentNodes, value));

        return false;
    }

    @Override
    public void clear() {
        entriesByCardinality.clear();
    }

    /**
     * Checks if the 'child' BitSet is a subset of the 'parent' BitSet.
     */
    private boolean isSubset(BitSet child, BitSet parent) {
        for (int i = child.nextSetBit(0); i >= 0; i = child.nextSetBit(i + 1)) {
            if (!parent.get(i)) {
                return false;
            }
        }
        return true;
    }

    private record DominanceEntry(BitSet remainingNodes, double value) {
        DominanceEntry(BitSet remainingNodes, double value) {
            this.remainingNodes = (BitSet) remainingNodes.clone();
            this.value = value;
        }
    }
}