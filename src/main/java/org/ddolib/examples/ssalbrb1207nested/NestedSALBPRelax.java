package org.ddolib.examples.ssalbrb1207nested;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.Relaxation;

import java.util.*;

/**
 * Relaxation of outer problem: merge states
 * 
 * Merge strategy (create true Relax state):
 * - completedTasks: take intersection (only tasks all states consider completed)
 * - currentStationTasks: take intersection (only tasks all states consider in current station)
 * - maybeCompletedTasks: take union minus intersection (tasks in some states completed or in current station, but not all)
 * - currentStationHasRobot: take OR (if any is true then true, larger capacity)
 * - usedRobots: take minimum (more remaining robots, more flexibility)
 */
public class NestedSALBPRelax implements Relaxation<NestedSALBPState> {

    @Override
    public NestedSALBPState mergeStates(Iterator<NestedSALBPState> states) {
        if (!states.hasNext()) {
            throw new IllegalArgumentException("Cannot merge empty state set");
        }

        List<NestedSALBPState> stateList = new ArrayList<>();
        states.forEachRemaining(stateList::add);
        
        if (stateList.size() == 1) {
            return stateList.get(0);  // Only one state, return directly
        }

        // ========== 1. completedTasks: take intersection (conservative) ==========
        Set<Integer> mergedCompleted = new LinkedHashSet<>(stateList.get(0).completedTasks());
        for (int i = 1; i < stateList.size(); i++) {
            mergedCompleted.retainAll(stateList.get(i).completedTasks());
        }

        // ========== 2. currentStationTasks: take intersection (optimistic) ==========
        Set<Integer> mergedCurrent = new LinkedHashSet<>(stateList.get(0).currentStationTasks());
        for (int i = 1; i < stateList.size(); i++) {
            mergedCurrent.retainAll(stateList.get(i).currentStationTasks());
        }

        // ========== 3. maybeCompletedTasks: union minus intersection ==========
        // Collect all assigned tasks (including original maybeCompletedTasks)
        Set<Integer> allAssigned = new LinkedHashSet<>();
        for (NestedSALBPState s : stateList) {
            allAssigned.addAll(s.completedTasks());
            allAssigned.addAll(s.currentStationTasks());
            allAssigned.addAll(s.maybeCompletedTasks());
        }

        // maybeCompletedTasks = all assigned tasks - confirmed completed - confirmed in current station
        Set<Integer> mergedMaybe = new LinkedHashSet<>(allAssigned);
        mergedMaybe.removeAll(mergedCompleted);
        mergedMaybe.removeAll(mergedCurrent);

        // ========== 4. currentStationHasRobot: take OR (if any is true then true) ==========
        boolean mergedHasRobot = stateList.stream()
            .anyMatch(NestedSALBPState::currentStationHasRobot);

        // ========== 5. usedRobots: take minimum (most optimistic) ==========
        int mergedUsedRobots = stateList.stream()
            .mapToInt(NestedSALBPState::usedRobots)
            .min()
            .orElse(0);

        return new NestedSALBPState(
            mergedCompleted,
            mergedCurrent,
            mergedMaybe,
            mergedHasRobot,
            mergedUsedRobots
        );
    }

    @Override
    public double relaxEdge(NestedSALBPState from,
                            NestedSALBPState to,
                            NestedSALBPState merged,
                            Decision decision,
                            double originalCost) {
        return originalCost;
    }
}
