package org.ddolib.nolayer.examples.tsp;

import org.ddolib.nolayer.modeling.NoLayerDominanceChecker;

import java.util.*;

public class TSPNoLayerDominanceChecker implements NoLayerDominanceChecker<TSPState> {

    private final Map<Integer, List<Entry>> entriesByCardinality = new HashMap<>();

    @Override
    public boolean updateDominance(TSPState state, double value) {
        int card = state.toVisit.cardinality();

        // state is dominated if there's an entry with:
        // entry.value <= value
        // entry.current == state.current
        // entry.toVisit is subset of state.toVisit
        List<Entry> entries = entriesByCardinality.get(card);
        if (entries != null) {
            for (Entry entry : entries) {
                if (entry.value <= value && entry.current.equals(state.current) && entry.toVisit.equals(state.toVisit)) {
                    return true;
                }
            }
        }

        // Add state to entries
        entriesByCardinality.computeIfAbsent(card, k -> new ArrayList<>())
                .add(new Entry(state.current, state.toVisit, value));

        List<Entry> entriesToRemove = entriesByCardinality.get(card);
        if (entriesToRemove != null) {
            entriesToRemove.removeIf(entry -> value <= entry.value && entry.current.equals(state.current) && entry.toVisit.equals(state.toVisit));
        }

        return false;
    }

    @Override
    public void clear() {
        entriesByCardinality.clear();
    }

    private static class Entry {
        BitSet current;
        BitSet toVisit;
        double value;

        Entry(BitSet current, BitSet toVisit, double value) {
            this.current = (BitSet) current.clone();
            this.toVisit = (BitSet) toVisit.clone();
            this.value = value;
        }
    }
}
