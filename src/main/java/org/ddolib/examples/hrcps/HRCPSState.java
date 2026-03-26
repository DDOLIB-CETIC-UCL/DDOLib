package org.ddolib.examples.hrcps;

import java.util.HashSet;
import java.util.Set;

/**
 * State of the outer (station-assignment) problem in the HRCPS model.
 * <p>
 * Every station has both a human worker and a robot, so there are no
 * robot-allocation fields.
 */
public record HRCPSState(
        Set<Integer> completedTasks,
        Set<Integer> currentStationTasks,
        Set<Integer> maybeCompletedTasks) {

    public HRCPSState {
        completedTasks = Set.copyOf(completedTasks);
        currentStationTasks = Set.copyOf(currentStationTasks);
        maybeCompletedTasks = Set.copyOf(maybeCompletedTasks);

        Set<Integer> inter1 = new HashSet<>(completedTasks);
        inter1.retainAll(currentStationTasks);
        if (!inter1.isEmpty())
            throw new IllegalArgumentException("completedTasks and currentStationTasks must be disjoint");

        Set<Integer> inter2 = new HashSet<>(completedTasks);
        inter2.retainAll(maybeCompletedTasks);
        if (!inter2.isEmpty())
            throw new IllegalArgumentException("completedTasks and maybeCompletedTasks must be disjoint");

        Set<Integer> inter3 = new HashSet<>(currentStationTasks);
        inter3.retainAll(maybeCompletedTasks);
        if (!inter3.isEmpty())
            throw new IllegalArgumentException("currentStationTasks and maybeCompletedTasks must be disjoint");
    }

    public boolean isComplete(int totalTasks) {
        return completedTasks.size() + currentStationTasks.size() == totalTasks;
    }

    public Set<Integer> getRemainingTasks(int totalTasks) {
        Set<Integer> remaining = new java.util.LinkedHashSet<>();
        for (int i = 0; i < totalTasks; i++) remaining.add(i);
        remaining.removeAll(completedTasks);
        remaining.removeAll(currentStationTasks);
        return remaining;
    }

    public Set<Integer> getRemainingTasksForLowerBound(int totalTasks) {
        Set<Integer> remaining = new java.util.LinkedHashSet<>();
        for (int i = 0; i < totalTasks; i++) remaining.add(i);
        remaining.removeAll(completedTasks);
        remaining.removeAll(currentStationTasks);
        remaining.removeAll(maybeCompletedTasks);
        return remaining;
    }

    @Override
    public String toString() {
        return String.format("<Completed=%d, CS=%s, Maybe=%d>",
                completedTasks.size(), currentStationTasks, maybeCompletedTasks.size());
    }
}

