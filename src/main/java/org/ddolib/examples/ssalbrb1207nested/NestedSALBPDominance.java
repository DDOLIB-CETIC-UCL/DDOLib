package org.ddolib.examples.ssalbrb1207nested;

import org.ddolib.modeling.Dominance;
import java.util.HashSet;
import java.util.Set;

/**
 * Dominance Rule - Memory-based Dominance Rule
 *
 * State dominance rule for pruning: when one state is dominated by another, 
 * the dominated state can be safely discarded.
 *
 * Dominance rule design:
 *
 * Prerequisite:
 *   - Already assigned tasks (completedTasks ∪ currentStationTasks ∪ maybeCompletedTasks) are the same
 *
 * Dominance judgment rules:
 *   Rule 1 - Robot resources: state with more remaining robots dominates state with fewer remaining robots
 *            (because it has more future choices)
 *
 *   Rule 2 - Robot advantage: when total robots used equal, state with robot in current station dominates state without
 *            (because station with robot can accept more tasks, larger search space)
 *
 *   Combined rule: s2 dominates s1 if and only if s2's "total robots used" not more than s1's
 *                 AND s2's current station robot status not worse than s1's
 *
 * Improvement explanation:
 *   Compared to strict version (require completedTasks and currentStationTasks exactly the same),
 *   relaxed version only requires "assigned task set" to be the same, can compare more state pairs, stronger pruning effect.
 *
 * Theoretical correctness:
 *   If two states have the same "assigned task set", they face the same "remaining task assignment problem".
 *   Then, state using fewer robots has more future choices, therefore not worse.
 */
public class NestedSALBPDominance implements Dominance<NestedSALBPState> {

    /**
     * Grouping key for dominance relationship
     *
     * Strategy choices:
     * - Strict strategy: compare only if completedTasks + currentStationTasks are exactly the same
     * - Relaxed strategy: compare if "all assigned tasks" are the same
     *
     * Current use: Relaxed strategy (Memory-based Dominance Rule)
     */
    private record DominanceKey(
            Set<Integer> allAssignedTasks  // completedTasks ∪ currentStationTasks ∪ maybeCompletedTasks
    ) {}

    /**
     * Return key for grouping.
     * Only states with the same key will be compared for dominance relationship.
     *
     * Uses improved Memory-based Dominance Rule from literature:
     * As long as "assigned task set" is the same, perform dominance comparison,
     * regardless of how these tasks are distributed to stations.
     *
     * @param state Current state
     * @return Grouping key (all assigned tasks)
     */
    @Override
    public Object getKey(NestedSALBPState state) {
        // Merge completedTasks, currentStationTasks and maybeCompletedTasks
        Set<Integer> allAssigned = new HashSet<>(state.completedTasks());
        allAssigned.addAll(state.currentStationTasks());
        allAssigned.addAll(state.maybeCompletedTasks());
        return new DominanceKey(allAssigned);
    }

    /**
     * @param state1 First state
     * @param state2 Second state
     * @return true if state1 is dominated by state2 or they are equivalent
     */
    @Override
    public boolean isDominatedOrEqual(NestedSALBPState state1, NestedSALBPState state2) {
        // Compute total robots used (including current station)
        int totalUsed1 = state1.usedRobots() + (state1.currentStationHasRobot() ? 1 : 0);
        int totalUsed2 = state2.usedRobots() + (state2.currentStationHasRobot() ? 1 : 0);

        // Rule 2: state2 uses fewer robots -> state2 is better -> state1 is dominated
        if (totalUsed2 < totalUsed1) {
            return true;
        }

        // Rule 1: when total robots used equal, compare current station robot status
        if (totalUsed1 == totalUsed2) {
            // Case when usedRobots is equal
            if (state1.usedRobots() == state2.usedRobots()) {
                // If state2 has robot but state1 doesn't -> state1 is dominated
                // If both have same robot status -> equivalent
                // If state1 has robot but state2 doesn't -> state1 is not dominated
                if (!state1.currentStationHasRobot() && state2.currentStationHasRobot()) {
                    return true;  // state1 is dominated by state2
                }
                if (state1.currentStationHasRobot() == state2.currentStationHasRobot()) {
                    return true;  // equivalent
                }
            }
            // Case when usedRobots not equal but totalUsed equal
            // E.g.: state1(usedRobots=2, hasRobot=false) vs state2(usedRobots=1, hasRobot=true)
            // Both totalUsed=2, but state2 has robot currently, more flexible in future
            // In this case state2 slightly better, but not strict dominance, conservatively don't prune
        }

        return false;
    }
}
