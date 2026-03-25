package org.ddolib.examples.ssalbrb1207nested;

import org.ddolib.modeling.StateRanking;

/**
 * Outer state ranking: prioritize states with fewer stations
 */
public class NestedSALBPRanking implements StateRanking<NestedSALBPState> {

    private final int totalRobots;

    public NestedSALBPRanking(int totalRobots) {
        this.totalRobots = totalRobots;
    }

    @Override
    public int compare(NestedSALBPState first, NestedSALBPState second) {
        // First compare number of completed tasks (fewer is better, approximates station count)
        int completedCompare = Integer.compare(
                first.completedTasks().size(),
                second.completedTasks().size()
        );
        if (completedCompare != 0) {
            return completedCompare;
        }

        // Next compare current station task count (more is better - fitting more tasks in current station means fewer total stations)
        int currentTasksCompare = Integer.compare(
                second.currentStationTasks().size(),  // Note: second first means larger is better
                first.currentStationTasks().size()
        );
        if (currentTasksCompare != 0) {
            return currentTasksCompare;
        }

        // Third, compare maybeCompletedTasks count (more is better - means more tasks may be completed)
        int maybeCompare = Integer.compare(
                second.maybeCompletedTasks().size(),  // Note: second first means larger is better
                first.maybeCompletedTasks().size()
        );
        if (maybeCompare != 0) {
            return maybeCompare;
        }

        // Fourth, if current station has robot, prioritize (increases current station capacity, can fit more tasks)
        int currentRobotCompare = Boolean.compare(
                second.currentStationHasRobot(),  // Note order: true > false
                first.currentStationHasRobot()
        );
        if (currentRobotCompare != 0) {
            return currentRobotCompare;
        }

        // Fifth, compare robots used (fewer is better - preserve more robots for future)
        int usedRobotsCompare = Integer.compare(
                first.usedRobots(),  // Note order: first means smaller is better
                second.usedRobots()
        );
        if (usedRobotsCompare != 0) {
            return usedRobotsCompare;
        }

        // Finally compare remaining robots (more is better)
        return Integer.compare(second.remainingRobots(totalRobots), first.remainingRobots(totalRobots));
    }
}
