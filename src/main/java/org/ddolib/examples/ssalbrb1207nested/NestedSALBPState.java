package org.ddolib.examples.ssalbrb1207nested;

import java.util.HashSet;
import java.util.Set;

/**
 * Nested SALBP State (supports robot allocation decisions) - Version supporting Relax
 *
 * State representation:
 * - completedTasks: Tasks confirmed completed in closed stations (Relax takes intersection)
 * - currentStationTasks: Tasks confirmed in current station (Relax takes intersection)
 * - maybeCompletedTasks: Tasks possibly completed or in current station (Relax takes union minus intersection)
 * - currentStationHasRobot: Whether current station has robot
 * - usedRobots: Number of robots used (includes closed stations, excludes current station)
 *
 * Purpose of maybeCompletedTasks:
 * - Only produced during Relax merge
 * - May decrease during transition (when assigning those tasks)
 * - Used for optimistic estimation in lower bound calculation
 */
public record NestedSALBPState(
        Set<Integer> completedTasks,                // Tasks confirmed completed
        Set<Integer> currentStationTasks,           // Tasks confirmed in current station
        Set<Integer> maybeCompletedTasks,           // Tasks possibly completed
        boolean currentStationHasRobot,             // Whether current station has robot
        int usedRobots) {                           // Number of robots used

    public NestedSALBPState {
        // Defensive copy
        completedTasks = Set.copyOf(completedTasks);
        currentStationTasks = Set.copyOf(currentStationTasks);
        maybeCompletedTasks = Set.copyOf(maybeCompletedTasks);
        
        // Invariant check: three sets should be mutually disjoint
        Set<Integer> intersection1 = new HashSet<>(completedTasks);
        intersection1.retainAll(currentStationTasks);
        if (!intersection1.isEmpty()) {
            throw new IllegalArgumentException(
                "completedTasks and currentStationTasks must be disjoint");
        }
        
        Set<Integer> intersection2 = new HashSet<>(completedTasks);
        intersection2.retainAll(maybeCompletedTasks);
        if (!intersection2.isEmpty()) {
            throw new IllegalArgumentException(
                "completedTasks and maybeCompletedTasks must be disjoint");
        }
        
        Set<Integer> intersection3 = new HashSet<>(currentStationTasks);
        intersection3.retainAll(maybeCompletedTasks);
        if (!intersection3.isEmpty()) {
            throw new IllegalArgumentException(
                "currentStationTasks and maybeCompletedTasks must be disjoint");
        }
    }

    /**
     * Check if all tasks are allocated
     * 
     * Key point: Does NOT include maybeCompletedTasks
     * Because these tasks may still need to be reassigned
     */
    public boolean isComplete(int totalTasks) {
        return completedTasks.size() + currentStationTasks.size() == totalTasks;
    }

    /**
     * Get remaining tasks set (for domain generation)
     * 
     * Key point: Tasks in maybeCompletedTasks are STILL in remaining!
     * Because these tasks may not be completed yet, need continued assignment
     */
    public Set<Integer> getRemainingTasks(int totalTasks) {
        java.util.Set<Integer> remaining = new java.util.LinkedHashSet<>();
        for (int i = 0; i < totalTasks; i++) {
            remaining.add(i);
        }
        // Only remove tasks confirmed completed and in current station
        remaining.removeAll(completedTasks);
        remaining.removeAll(currentStationTasks);
        // DO NOT remove maybeCompletedTasks (these tasks may still need assignment)
        return remaining;
    }

    /**
     * Get remaining tasks set (for lower bound calculation)
     * 
     * Key point: Tasks in maybeCompletedTasks are NOT in this set!
     * Because for lower bound calculation, we optimistically assume these tasks are efficiently completed
     */
    public Set<Integer> getRemainingTasksForLowerBound(int totalTasks) {
        java.util.Set<Integer> remaining = new java.util.LinkedHashSet<>();
        for (int i = 0; i < totalTasks; i++) {
            remaining.add(i);
        }
        // Remove all assigned tasks (including maybe)
        remaining.removeAll(completedTasks);
        remaining.removeAll(currentStationTasks);
        remaining.removeAll(maybeCompletedTasks);  // Remove maybe for lower bound calculation
        return remaining;
    }

    /**
     * Remaining robots (needs total robots count to be passed in)
     */
    public int remainingRobots(int totalRobots) {
        int currentUsed = currentStationHasRobot() ? 1 : 0;
        return Math.max(0, totalRobots - usedRobots() - currentUsed);
    }

    @Override
    public String toString() {
        return String.format("<Completed=%d, CS=%s, Maybe=%d, CSRobot=%s, usedRobots=%d>",
                completedTasks.size(), currentStationTasks,
                maybeCompletedTasks.size(),
                currentStationHasRobot ? "Y" : "N",
                usedRobots);
    }
}
