package org.ddolib.examples.nolayer.tsp;

import org.ddolib.nolayer.modeling.NoLayerDominanceChecker;

import java.util.*;

/**
 * Dominance checker for the Travelling Salesperson Problem (TSP).
 * Correctly implements subset dominance relations across different cardinalities.
 */
public class TSPNoLayerDominanceChecker implements NoLayerDominanceChecker<TSPState> {

    private final Map<Integer, List<DominanceEntry>> fronts = new HashMap<>();

    @Override
    public boolean updateDominance(TSPState state, double value) {
        BitSet toVisit = state.toVisit;
        BitSet current = state.current;
        int card = toVisit.cardinality();

        // 1. Check if the new 'state' is dominated by any existing entry
        // An entry can dominate 'state' only if it has AT MOST as many cities left to visit (smaller or equal cardinality)
        for (Map.Entry<Integer, List<DominanceEntry>> mapEntry : fronts.entrySet()) {
            if (mapEntry.getKey() <= card) {
                for (DominanceEntry entry : mapEntry.getValue()) {
                    if (entry.value <= value
                            && entry.current.equals(current)
                            && isSubset(entry.toVisit, toVisit)) {
                        return true; // The new state is dominated, discard it
                    }
                }
            }
        }

        // 2. Remove existing entries that are dominated by the new 'state'
        // The new state can dominate an entry only if it has AT LEAST as many cities left to visit (larger or equal cardinality)
        for (Map.Entry<Integer, List<DominanceEntry>> mapEntry : fronts.entrySet()) {
            if (mapEntry.getKey() >= card) {
                mapEntry.getValue().removeIf(entry ->
                        value <= entry.value
                                && current.equals(entry.current)
                                && isSubset(toVisit, entry.toVisit)
                );
            }
        }

        // 3. Add the new state to the matching cardinality front
        fronts.computeIfAbsent(card, k -> new ArrayList<>())
                .add(new DominanceEntry(current, toVisit, value));

        return false;
    }

    @Override
    public void clear() {
        fronts.clear();
    }

    /**
     * Checks if the 'child' BitSet is a subset of the 'parent' BitSet.
     * Performance optimized loop without object allocations.
     */
    private boolean isSubset(BitSet child, BitSet parent) {
        for (int i = child.nextSetBit(0); i >= 0; i = child.nextSetBit(i + 1)) {
            if (!parent.get(i)) {
                return false;
            }
        }
        return true;
    }

    private record DominanceEntry(BitSet current, BitSet toVisit, double value) {
        private DominanceEntry(BitSet current, BitSet toVisit, double value) {
            this.current = (BitSet) current.clone();
            this.toVisit = (BitSet) toVisit.clone();
            this.value = value;
        }
    }
}