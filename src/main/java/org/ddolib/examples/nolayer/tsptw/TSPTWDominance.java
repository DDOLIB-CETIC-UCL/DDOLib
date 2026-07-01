package org.ddolib.examples.nolayer.tsptw;

import org.ddolib.common.dominance.NoLayerDominanceChecker;

import java.util.*;

public class TSPTWDominance implements NoLayerDominanceChecker<TSPTWState> {

    private final Map<DominanceKey, List<DominanceEntry>> dominanceFronts = new HashMap<>();

    @Override
    public boolean updateDominance(TSPTWState state, double value) {
        BitSet mustVisit = (BitSet) state.mustVisit().clone();
        DominanceKey key = new DominanceKey(state.currentCity(), mustVisit);

        List<DominanceEntry> front = dominanceFronts.get(key);
        if (front != null) {
            for (DominanceEntry entry : front) {
                if (entry.value() <= value && entry.time() <= state.time()) {
                    return true;
                }
            }
            front.removeIf(entry -> value <= entry.value() && state.time() <= entry.time());
        }
        DominanceEntry newEntry = new DominanceEntry(state.time(), value);
        dominanceFronts.computeIfAbsent(key, k -> new ArrayList<>()).add(newEntry);

        return false;
    }

    @Override
    public void clear() {
        dominanceFronts.clear();
    }

    private record DominanceKey(int currentCity, BitSet mustVisit) {
    }

    private record DominanceEntry(int time, double value) {
    }
}
