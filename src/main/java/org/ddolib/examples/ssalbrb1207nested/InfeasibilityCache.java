package org.ddolib.examples.ssalbrb1207nested;

import java.util.*;

/**
 * Infeasible task subset cache
 * Used for pruning: if task set S is infeasible, then any superset containing S is also infeasible
 *
 * Optimization strategies:
 * 1. Record minimal infeasible task subsets
 * 2. Before checking feasibility, first check if it contains known infeasible subsets
 * 3. Reduce number of inner DDO calls
 */
public class InfeasibilityCache {

    // Store infeasible task subsets (grouped by size for fast lookup)
    private final Map<Integer, Set<Set<Integer>>> infeasibleSets;

    // Statistics
    private long pruneCount = 0;
    private long checkCount = 0;

    public InfeasibilityCache() {
        this.infeasibleSets = new HashMap<>();
    }

    /**
     * Record an infeasible task set
     * Only records minimal infeasible subsets
     *
     * @param tasks Infeasible task set
     * @param hasRobot Whether robot is available
     */
    public void recordInfeasible(Set<Integer> tasks, boolean hasRobot) {
        if (tasks.isEmpty()) return;

        // Only record infeasible sets with robot
        // Because infeasibility without robot cannot be generalized to with robot
        if (!hasRobot) return;

        // Check if there is already a smaller infeasible subset
        if (containsInfeasibleSubset(tasks)) {
            return; // Already have smaller infeasible subset, no need to record
        }

        // Remove all supersets of current set (they are redundant)
        removeSupersetsOf(tasks);

        // Record current infeasible set
        int size = tasks.size();
        infeasibleSets.computeIfAbsent(size, k -> new HashSet<>()).add(Set.copyOf(tasks));
    }

    /**
     * Check if task set contains known infeasible subsets
     *
     * @param tasks Task set to check
     * @return true if contains infeasible subset (can prune)
     */
    public boolean containsInfeasibleSubset(Set<Integer> tasks) {
        checkCount++;

        if (tasks.isEmpty()) return false;

        // Only need to check infeasible sets with size <= tasks.size()
        for (int size = 1; size <= tasks.size(); size++) {
            Set<Set<Integer>> setsOfSize = infeasibleSets.get(size);
            if (setsOfSize == null) continue;

            for (Set<Integer> infeasibleSet : setsOfSize) {
                // Check if infeasibleSet is subset of tasks
                if (tasks.containsAll(infeasibleSet)) {
                    pruneCount++;
                    return true; // Found infeasible subset, can prune
                }
            }
        }

        return false;
    }

    /**
     * Remove all supersets of given set (they are redundant)
     */
    private void removeSupersetsOf(Set<Integer> tasks) {
        for (int size = tasks.size() + 1; size <= 20; size++) { // Assume max station tasks <= 20
            Set<Set<Integer>> setsOfSize = infeasibleSets.get(size);
            if (setsOfSize == null) continue;

            setsOfSize.removeIf(set -> set.containsAll(tasks));
        }
    }

    /**
     * Get prune count (for external statistics)
     */
    public long getPruneCount() {
        return pruneCount;
    }

    /**
     * Get check count (for external statistics)
     */
    public long getCheckCount() {
        return checkCount;
    }

    /**
     * Get number of stored infeasible sets
     */
    public int getStoredCount() {
        return infeasibleSets.values().stream()
                .mapToInt(Set::size)
                .sum();
    }

    /**
     * Get statistics information
     */
    public String getStatistics() {
        int totalInfeasibleSets = getStoredCount();

        return String.format("InfeasibilityCache: checks=%d, prunes=%d, stored=%d, pruneRate=%.2f%%",
                checkCount, pruneCount, totalInfeasibleSets,
                checkCount > 0 ? (100.0 * pruneCount / checkCount) : 0.0);
    }

    /**
     * Clear cache
     */
    public void clear() {
        infeasibleSets.clear();
        pruneCount = 0;
        checkCount = 0;
    }
}
