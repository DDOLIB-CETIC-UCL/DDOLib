package org.ddolib.examples.hrcps;

import java.util.*;

/**
 * Cache of known infeasible task subsets.
 * If task set S is infeasible, every superset of S is also infeasible.
 */
public class InfeasibilityCache {

    private final Map<Integer, Set<Set<Integer>>> infeasibleSets = new HashMap<>();
    private long pruneCount = 0;
    private long checkCount = 0;

    public void recordInfeasible(Set<Integer> tasks, boolean hasRobot) {
        if (tasks.isEmpty() || !hasRobot) return;
        if (containsInfeasibleSubset(tasks)) return;
        removeSupersetsOf(tasks);
        infeasibleSets.computeIfAbsent(tasks.size(), k -> new HashSet<>()).add(Set.copyOf(tasks));
    }

    public boolean containsInfeasibleSubset(Set<Integer> tasks) {
        checkCount++;
        if (tasks.isEmpty()) return false;
        for (int size = 1; size <= tasks.size(); size++) {
            Set<Set<Integer>> setsOfSize = infeasibleSets.get(size);
            if (setsOfSize == null) continue;
            for (Set<Integer> inf : setsOfSize) {
                if (tasks.containsAll(inf)) {
                    pruneCount++;
                    return true;
                }
            }
        }
        return false;
    }

    private void removeSupersetsOf(Set<Integer> tasks) {
        for (int size = tasks.size() + 1; size <= 20; size++) {
            Set<Set<Integer>> setsOfSize = infeasibleSets.get(size);
            if (setsOfSize != null) setsOfSize.removeIf(s -> s.containsAll(tasks));
        }
    }

    public long getPruneCount() { return pruneCount; }
    public long getCheckCount() { return checkCount; }

    public int getStoredCount() {
        return infeasibleSets.values().stream().mapToInt(Set::size).sum();
    }

    public String getStatistics() {
        return String.format("InfeasibilityCache: checks=%d, prunes=%d, stored=%d, rate=%.2f%%",
                checkCount, pruneCount, getStoredCount(),
                checkCount > 0 ? (100.0 * pruneCount / checkCount) : 0.0);
    }

    public void clear() {
        infeasibleSets.clear();
        pruneCount = checkCount = 0;
    }
}

