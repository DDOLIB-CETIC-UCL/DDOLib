package org.ddolib.examples.hrcps;

import org.ddolib.modeling.FastLowerBound;

import java.util.*;

/**
 * Admissible lower bound on the number of stations still needed.
 * <p>
 * Since every station has both a human and a robot, the effective capacity
 * per station is bounded by the two-resource makespan constraint.
 * <p>
 * <b>LB1 (workload bound):</b><br>
 * {@code totalWork = Σ min(h[i], r[i], 2·c[i])}<br>
 * Each station can handle at most {@code 2·cycleTime} total work
 * (human does ≤ cycleTime, robot does ≤ cycleTime).<br>
 * {@code LB1 = ⌈totalWork / (2·cycleTime)⌉}
 * <p>
 * <b>LB2 (single-resource bound):</b><br>
 * For tasks that <em>cannot</em> use the robot (robotDur ≥ BIG_M, collabDur ≥ BIG_M),
 * the human must process them alone. Their total human time must fit into stations.<br>
 * {@code LB2 = ⌈Σ h[i] (human-only tasks) / cycleTime⌉}<br>
 * Symmetrically for robot-only tasks.
 */
public class HRCPSFastLowerBound implements FastLowerBound<HRCPSState> {

    private static final int BIG_M = 100_000;

    private final int cycleTime;
    private final int nbTasks;
    private final int[] humanDur;
    private final int[] robotDur;
    private final int[] collabDur;

    public HRCPSFastLowerBound(HRCPSProblem problem) {
        this.cycleTime = problem.cycleTime;
        this.nbTasks = problem.nbTasks;
        this.humanDur = problem.innerProblem.humanDurations;
        this.robotDur = problem.innerProblem.robotDurations;
        this.collabDur = problem.innerProblem.collaborationDurations;
    }

    @Override
    public double fastLowerBound(HRCPSState state, Set<Integer> variables) {
        if (state.isComplete(nbTasks)) return 0;

        Set<Integer> remaining = state.getRemainingTasksForLowerBound(nbTasks);
        Set<Integer> current = state.currentStationTasks();

        if (remaining.isEmpty() && current.isEmpty()) return 0;
        if (remaining.isEmpty()) return 0;

        if (current.isEmpty()) {
            return computeLowerBound(remaining);
        }

        Set<Integer> allUnfinished = new HashSet<>(current);
        allUnfinished.addAll(remaining);
        double total = computeLowerBound(allUnfinished);
        return Math.max(0, total - 1);
    }

    /**
     * Compute lower bound on stations needed for a set of tasks.
     */
    public double computeLowerBound(Set<Integer> tasks) {
        if (tasks.isEmpty()) return 0;

        double lb1 = computeWorkloadBound(tasks);
        double lb2 = computeSingleResourceBound(tasks);
        return Math.max(lb1, lb2);
    }

    /** LB1: workload bound using two-resource capacity. */
    private double computeWorkloadBound(Set<Integer> tasks) {
        double totalWork = 0;
        for (int t : tasks) {
            totalWork += Math.min(humanDur[t],
                    Math.min(robotDur[t], 2.0 * collabDur[t]));
        }
        return Math.ceil(totalWork / (2.0 * cycleTime));
    }

    /** LB2: single-resource bounds for tasks locked to one resource. */
    private double computeSingleResourceBound(Set<Integer> tasks) {
        double humanOnly = 0;
        double robotOnly = 0;
        for (int t : tasks) {
            boolean canRobot = robotDur[t] < BIG_M;
            boolean canCollab = collabDur[t] < BIG_M;
            boolean canHuman = humanDur[t] < BIG_M;

            if (!canRobot && !canCollab && canHuman) {
                humanOnly += humanDur[t];
            }
            if (!canHuman && !canCollab && canRobot) {
                robotOnly += robotDur[t];
            }
        }
        double lbH = Math.ceil(humanOnly / cycleTime);
        double lbR = Math.ceil(robotOnly / cycleTime);
        return Math.max(lbH, lbR);
    }
}

