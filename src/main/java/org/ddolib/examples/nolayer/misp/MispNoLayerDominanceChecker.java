package org.ddolib.examples.nolayer.misp;

import org.ddolib.common.dominance.NoLayerDominanceChecker;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MispNoLayerDominanceChecker implements NoLayerDominanceChecker<MispState> {

    private static class Entry {
        BitSet remainingNodes;
        double value;

        Entry(BitSet remainingNodes, double value) {
            this.remainingNodes = (BitSet) remainingNodes.clone();
            this.value = value;
        }
    }

    private final Map<Integer, List<Entry>> entriesByCardinality = new HashMap<>();

    @Override
    public boolean updateDominance(MispState state, double value) {
        BitSet s2 = state.remainingNodes();
        int s2Card = s2.cardinality();
        
        // Check if state is dominated by any existing entry
        // A state can only be dominated by an entry with AT LEAST as many remaining nodes
        for (int card = s2Card; card <= s2.size(); card++) {
            List<Entry> entries = entriesByCardinality.get(card);
            if (entries == null) continue;
            for (Entry entry : entries) {
                if (entry.value <= value) { // entry is better or equal
                    BitSet s1 = entry.remainingNodes;
                    // s1 dominates s2 if all bits in s2 are also in s1
                    boolean dominated = true;
                    for (int i = s2.nextSetBit(0); i >= 0; i = s2.nextSetBit(i + 1)) {
                        if (!s1.get(i)) {
                            dominated = false;
                            break;
                        }
                    }
                    if (dominated) {
                        return true;
                    }
                }
            }
        }
        
        // Add to entries
        entriesByCardinality.computeIfAbsent(s2Card, k -> new ArrayList<>()).add(new Entry(s2, value));

        // Optional: remove entries that are dominated by the new state
        // A state can only dominate entries with AT MOST as many remaining nodes
        for (int card = 0; card <= s2Card; card++) {
            List<Entry> entries = entriesByCardinality.get(card);
            if (entries == null) continue;
            entries.removeIf(entry -> {
                if (value <= entry.value) {
                    BitSet s1 = s2; // new state
                    BitSet s = entry.remainingNodes;
                    boolean dominates = true;
                    for (int i = s.nextSetBit(0); i >= 0; i = s.nextSetBit(i + 1)) {
                        if (!s1.get(i)) {
                            dominates = false;
                            break;
                        }
                    }
                    return dominates;
                }
                return false;
            });
        }

        return false;
    }

    @Override
    public void clear() {
        entriesByCardinality.clear();
    }
}
