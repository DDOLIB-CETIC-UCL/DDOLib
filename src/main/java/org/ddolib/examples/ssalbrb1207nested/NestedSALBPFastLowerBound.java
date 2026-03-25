package org.ddolib.examples.ssalbrb1207nested;

import org.ddolib.modeling.FastLowerBound;
import org.ddolib.examples.ssalbrb.SSALBRBProblem;

import java.util.*;

/**
 * For the Type I Assembly Line Balancing Problem combined with human-robot collaborative resource allocation,
 * implemented two complementary lower bound calculation methods:
 *
 *   LB₁ (workload balancing lower bound):
 *   Based on conversion coefficients of tasks among different execution modes, estimates minimum stations needed through workload balancing principle
 *   LB₂ (improved bin packing lower bound):
 *   Classifies tasks into large, medium, small based on processing time, estimates minimum stations using bin packing strategy.
 *   Final lower bound takes maximum of both: LB = max(LB₁, LB₂)
 *
 * Core design principle:
 *  When current station is empty: directly calculate stations needed for remaining tasks
 *  When current station is not empty: calculate total stations needed for all incomplete tasks (current station + remaining),
 *  then subtract 1 (since current station already occupies 1 station)
 *
 */
public class NestedSALBPFastLowerBound implements FastLowerBound<NestedSALBPState> {

    // ==================== Problem Parameters ====================

    private final int cycleTime;      // Cycle time c
    private final int nbTasks;        // Total number of tasks n
    private final int totalRobots;    // Total available robots q

    // Task processing time arrays (precomputed for performance)
    private final int[] humanDur;     // Human time for each task t_ih
    private final int[] robotDur;     // Robot time for each task t_ir
    private final int[] collabDur;    // Cooperative time for each task t_ic

    // ==================== Constructor ====================

    /**
     * Construct fast lower bound estimator
     *
     * @param problem Nested SALBP problem instance
     */
    public NestedSALBPFastLowerBound(NestedSALBPProblem problem) {
        this.cycleTime = problem.cycleTime;
        this.nbTasks = problem.nbTasks;
        this.totalRobots = problem.totalRobots;

        // Precompute processing time for each task to avoid repeated access
        this.humanDur = new int[nbTasks];
        this.robotDur = new int[nbTasks];
        this.collabDur = new int[nbTasks];
        for (int i = 0; i < nbTasks; i++) {
            this.humanDur[i] = problem.innerProblem.humanDurations[i];
            this.robotDur[i] = problem.innerProblem.robotDurations[i];
            this.collabDur[i] = problem.innerProblem.collaborationDurations[i];
        }
    }

    // ==================== FastLowerBound Interface Implementation ====================

    /**
     * Calculate fast lower bound for given state
     *
     * Lower bound represents minimum number of new stations still needed from current state to goal state.
     *
     * @param state Current state
     * @param variables Set of variables to decide (not used in this implementation)
     * @return Lower bound value (minimum new stations needed)
     */
    @Override
    public double fastLowerBound(NestedSALBPState state, Set<Integer> variables) {
        // Termination condition: all tasks assigned
        if (state.isComplete(nbTasks)) {
            return 0;
        }

        // Get remaining unassigned tasks and current station tasks
        // Use special method: exclude maybeCompletedTasks in lower bound calculation
        Set<Integer> remainingTasks = state.getRemainingTasksForLowerBound(nbTasks);
        Set<Integer> currentStationTasks = state.currentStationTasks();

        // Special case: no remaining tasks to assign
        // Only need to close current station (if not empty), no additional new stations
        if (remainingTasks.isEmpty()) {
            return 0;
        }

        // Calculate remaining available robots
        int remainingRobots = totalRobots - state.usedRobots();
        if (state.currentStationHasRobot()) {
            remainingRobots--;  // Current station's robot also counts as used
        }

        // ========== Case 1: Current station is empty ==========
        if (currentStationTasks.isEmpty()) {
            double lb = computeLowerBound(remainingTasks, remainingRobots);

            // [Debug] Output detailed info only at root node
//            if (state.completedTasks().isEmpty()) {
//                System.out.println("Root node lower bound calculation:");
//                System.out.println("Remaining tasks: " + remainingTasks.size());
//                System.out.println("Available robots: " + remainingRobots);
//
//                double lb1 = computeLB1(remainingTasks, remainingRobots);
//                double lb2 = computeLB2(remainingTasks, remainingRobots);
//
//                System.out.println("\n--- Lower bound calculation results ---");
//                System.out.println("LB₁ (workload lower bound): " + lb1);
//                System.out.println("LB₂ (bin packing lower bound): " + lb2);
//            }

            return lb;
        }

        // ========== Case 2: Current station is not empty ==========
        // Strategy: calculate total stations needed for all unfinished tasks, then subtract current station
        Set<Integer> allUnfinishedTasks = new HashSet<>();
        allUnfinishedTasks.addAll(currentStationTasks);
        allUnfinishedTasks.addAll(remainingTasks);

        // Calculate total available robots (including current station's robot)
        int totalAvailableRobots = remainingRobots;
        if (state.currentStationHasRobot()) {
            totalAvailableRobots++;
        }

        double totalStationsNeeded = computeLowerBound(allUnfinishedTasks, totalAvailableRobots);

        // Subtract current station (since it already exists)
        return Math.max(0, totalStationsNeeded - 1);
    }

    // ==================== Core Lower Bound Calculation Methods ====================

    /**
     * Calculate minimum stations needed for given task set (public method)
     * This method can be called externally to calculate lower bound for any task set
     *
     * @param tasks Task set
     * @param availableRobots Available robots count
     * @return Minimum stations needed
     */
    public double computeLowerBound(Set<Integer> tasks, int availableRobots) {
        double lb1 = computeLB1(tasks, availableRobots);
        double lb2 = computeLB2(tasks, availableRobots);

        return Math.max(lb1, lb2);
    }

    /**
     * LB₁: Workload balancing lower bound
     * Based on conversion coefficients θ_i = min{θ_ir, θ_ic}, where:
     *   θ_ir = t_ir / t_ih (robot mode conversion coefficient)
     *   θ_ic = t_ic / (t_ih - t_ic) (cooperative mode conversion coefficient)
     * Algorithm idea: Balance human and robot workloads by converting some tasks
     * from human mode to robot or cooperative mode, to estimate minimum stations needed.
     *
     * @param remainingTasks Remaining task set
     * @param q Available robots count
     * @return LB₁ lower bound value
     */
    private double computeLB1(Set<Integer> remainingTasks, int q) {
        if (remainingTasks.isEmpty()) {
            return 0;
        }

        // Build task conversion info list
        List<TaskConversion> conversions = new ArrayList<>();
        double LH = 0.0;  // Initial human workload

        for (int task : remainingTasks) {
            int tH = humanDur[task];
            int tR = robotDur[task];
            int tC = collabDur[task];

            LH += tH;

            // Calculate conversion coefficients (avoid division by zero and invalid conversions)
            double theta_ir = (tR < 100000 && tH > 0) ?
                    ((double) tR / tH) : Double.POSITIVE_INFINITY;
            double theta_ic = (tC < 100000 && tH > tC) ?
                    ((double) tC / (tH - tC)) : Double.POSITIVE_INFINITY;

            conversions.add(new TaskConversion(task, theta_ir, theta_ic, tH, tR, tC));
        }

        // Sort by conversion coefficients ascending (prioritize high-efficiency conversions)
        conversions.sort(Comparator.comparingDouble(tc -> Math.min(tc.theta_ir, tc.theta_ic)));

        double LR = 0.0;  // Robot workload

        // Iteratively convert workload until balanced or resources exhausted
        for (TaskConversion tc : conversions) {
            // Stop condition (iii): if already balanced, stop
            if (LR >= LH) {
                break;
            }

            // Record LH and LR before conversion (for partial conversion rollback)
            double LH_before = LH;
            double LR_before = LR;

            // Choose mode with smaller conversion coefficient
            if (tc.theta_ir <= tc.theta_ic) {
                // Use robot mode
                if (tc.tR < 100000) {
                    // Execute full conversion
                    LH -= tc.tH;
                    LR += tc.tR;

                    // Check if exceeds capacity limit (stop condition ii)
                    // LR = c·q represents total capacity of all available robots
                    if (LR > cycleTime * q) {
                        // Exceeds capacity, partial conversion rollback to capacity limit
                        // alpha * t_ir = (c·q - LR_before)
                        if (tc.tR > 0) {
                            double alpha = (cycleTime * q - LR_before) / tc.tR;
                            alpha = Math.max(0.0, Math.min(1.0, alpha));
                            LH = LH_before - alpha * tc.tH;
                            LR = LR_before + alpha * tc.tR;
                        }
                        break;
                    }

                    // Check if exceeds balance point (stop condition iii)
                    if (LR > LH) {
                        // Exceeds balance, partial conversion rollback to balance point
                        // LH_before - alpha * t_ih = LR_before + alpha * t_ir
                        double denom = tc.tH + tc.tR;
                        if (denom > 0) {
                            double alpha = (LH_before - LR_before) / denom;
                            alpha = Math.max(0.0, Math.min(1.0, alpha));
                            LH = LH_before - alpha * tc.tH;
                            LR = LR_before + alpha * tc.tR;
                        }
                        break;
                    }
                }
            } else {
                // Use cooperative mode
                if (tc.tC < 100000) {
                    double dh = tc.tH - tc.tC;
                    double dr = tc.tC;

                    // Execute full conversion
                    LH -= dh;
                    LR += dr;

                    // Check if exceeds capacity limit (stop condition ii)
                    // LR = c·q 表示所有可用机器人的总容量
                    if (LR > cycleTime * q) {
                        // 超过容量，部分转回到容量上限
                        // alpha * dr = (c·q - LR_before)
                        if (dr > 0) {
                            double alpha = (cycleTime * q - LR_before) / dr;
                            alpha = Math.max(0.0, Math.min(1.0, alpha));
                            LH = LH_before - alpha * dh;
                            LR = LR_before + alpha * dr;
                        }
                        break;
                    }

                    // 检查是否超过平衡点（停止条件iii）
                    if (LR > LH) {
                        // 超过平衡，部分转回到平衡点
                        // LH_before - alpha * dh = LR_before + alpha * dr
                        double denom = dh + dr;
                        if (denom > 0) {
                            double alpha = (LH_before - LR_before) / denom;
                            alpha = Math.max(0.0, Math.min(1.0, alpha));
                            LH = LH_before - alpha * dh;
                            LR = LR_before + alpha * dr;
                        }
                        break;
                    }
                }
            }
        }

        // LB₁ = ⌈LH / c⌉
        return Math.ceil(LH / cycleTime);
    }

    /**
     * LB₂: Improved bin packing lower bound
     * Core idea:
     * Step 1: Let robots execute tasks first, consume robot capacity c·q
     * Step 2: Calculate remaining tasks (remove fully executed, need conversion time for partially executed)
     * Step 3: Perform bin packing on remaining tasks, estimate stations needed
     *
     * Key point: After robots partially execute task, remaining workload converts to human time as:
     * Robot mode: remaining robot time / 2 = human time (because tH = tR / 2)
     * Cooperative mode: remaining workload calculated proportionally
     * Bin packing strategy:
     *  Large tasks: t > c/2, each occupies one bin (station)
     *  Medium tasks: c/3 < t ≤ c/2, prioritize filling remaining capacity of large task bins, pair if cannot fill
     *  Small tasks: t ≤ c/3, fill all remaining capacity
     *
     * @param remainingTasks Remaining task set
     * @param q Available robots count
     * @return LB₂ lower bound value
     */
    private double computeLB2(Set<Integer> remainingTasks, int q) {
        if (remainingTasks.isEmpty()) {
            return 0;
        }

        int c = cycleTime;

        // ========== Step 1: Let robots execute tasks first ==========
        // Total robot capacity
        double robotCapacity = c * q;

        // Sort by conversion coefficient (same order as LB₁)
        // Prioritize letting robots execute high-conversion-efficiency tasks
        List<TaskRobotInfo> tasksByRobotTime = new ArrayList<>();
        for (int task : remainingTasks) {
            int tH = humanDur[task];
            int tR = robotDur[task];
            int tC = collabDur[task];

            // Calculate conversion coefficient
            double theta_ir = (tR < 100000 && tH > 0) ? ((double) tR / tH) : Double.POSITIVE_INFINITY;
            double theta_ic = (tC < 100000 && tH > tC) ? ((double) tC / (tH - tC)) : Double.POSITIVE_INFINITY;
            double theta = Math.min(theta_ir, theta_ic);

            // Choose better robot execution mode
            boolean useRobotMode = (theta_ir <= theta_ic);
            int robotTime = useRobotMode ? tR : tC;

            tasksByRobotTime.add(new TaskRobotInfo(task, tH, tR, tC, robotTime, theta, useRobotMode));
        }
        // Sort by conversion coefficient ascending (consistent with LB₁)
        tasksByRobotTime.sort(Comparator.comparingDouble(t -> t.theta));

        // Let robots execute tasks until capacity is exhausted
        double usedRobotCapacity = 0.0;
        Map<Integer, Double> remainingHumanTime = new HashMap<>();

        for (TaskRobotInfo tri : tasksByRobotTime) {
            if (tri.robotTime >= 100000) {
                // Robot cannot execute this task
                remainingHumanTime.put(tri.task, (double) tri.humanTime);
                continue;
            }

            double availableCapacity = robotCapacity - usedRobotCapacity;

            if (availableCapacity <= 0) {
                // Robot capacity exhausted, remaining tasks fully executed by humans
                remainingHumanTime.put(tri.task, (double) tri.humanTime);
            } else if (tri.robotTime <= availableCapacity) {
                // Robot can completely execute this task
                usedRobotCapacity += tri.robotTime;
                // This task doesn't need human time anymore (fully executed by robot)
            } else {
                // Key point: Robot can only partially execute this task
                // Robot executed availableCapacity, remaining needs conversion to human time

                double robotExecuted = availableCapacity;  // Robot executed time
                double robotRemaining = tri.robotTime - robotExecuted;  // Robot remaining time
                double humanTimeForRemaining;

                if (tri.useRobotMode) {
                    // Robot mode: tH = tR / 2
                    // Remaining robot time converts to human time: robotRemaining / 2
                    humanTimeForRemaining = robotRemaining / 2.0;
                } else {
                    // Cooperative mode: remaining workload calculated proportionally
                    // Robot execution ratio (how much proportion of workload completed)
                    double robotRatio = robotExecuted / tri.robotTime;
                    // Remaining workload ratio (how much proportion remaining)
                    double humanRatio = 1.0 - robotRatio;
                    // Remaining human time = original cooperative time × remaining ratio
                    humanTimeForRemaining = tri.collabTime * humanRatio;
                }

                remainingHumanTime.put(tri.task, humanTimeForRemaining);
                usedRobotCapacity = robotCapacity; // Capacity exhausted
            }
        }

        // ========== Step 2: Perform bin packing on remaining tasks ==========
        if (remainingHumanTime.isEmpty()) {
            // All tasks executed by robots
            return 0;
        }

        // Classify tasks
        List<Integer> largeTasks = new ArrayList<>();
        List<Integer> mediumTasks = new ArrayList<>();
        List<Integer> smallTasks = new ArrayList<>();

        for (Map.Entry<Integer, Double> entry : remainingHumanTime.entrySet()) {
            int task = entry.getKey();
            double taskTime = entry.getValue();

            if (taskTime > c / 2.0) {
                largeTasks.add(task);
            } else if (taskTime > c / 3.0) {
                mediumTasks.add(task);
            } else {
                smallTasks.add(task);
            }
        }

        // D₁: Number of bins for large tasks
        int d1 = largeTasks.size();
        double remainingCapacityD1 = 0.0;
        for (int task : largeTasks) {
            remainingCapacityD1 += (c - remainingHumanTime.get(task));
        }

        // D₂: Number of bins for medium tasks
        mediumTasks.sort(Comparator.comparingDouble(t -> remainingHumanTime.get(t)));

        // Try to assign medium tasks to remaining capacity of D₁
        double usedCapacityD1 = 0.0;
        List<Integer> unassignedMedium = new ArrayList<>();

        for (int task : mediumTasks) {
            double taskTime = remainingHumanTime.get(task);
            if (usedCapacityD1 + taskTime <= remainingCapacityD1) {
                usedCapacityD1 += taskTime;
            } else {
                unassignedMedium.add(task);
            }
        }

        // Bin packing unassigned medium tasks (at most 2 tasks per bin)
        int d2 = 0;
        double remainingCapacityD2 = 0.0;

        if (!unassignedMedium.isEmpty()) {
            int i = 0;
            while (i < unassignedMedium.size()) {
                int task1 = unassignedMedium.get(i);
                double t1 = remainingHumanTime.get(task1);

                if (i + 1 < unassignedMedium.size()) {
                    int task2 = unassignedMedium.get(i + 1);
                    double t2 = remainingHumanTime.get(task2);

                    if (t1 + t2 <= c) {
                        // Two tasks in one bin
                        d2++;
                        remainingCapacityD2 += (c - t1 - t2);
                        i += 2;
                    } else {
                        // Only one task
                        d2++;
                        remainingCapacityD2 += (c - t1);
                        i++;
                    }
                } else {
                    // Last task
                    d2++;
                    remainingCapacityD2 += (c - t1);
                    i++;
                }
            }
        }

        // d₃: Number of bins needed for small tasks
        double Ws = 0.0;
        for (int task : smallTasks) {
            Ws += remainingHumanTime.get(task);
        }

        double Wr = remainingCapacityD1 - usedCapacityD1 + remainingCapacityD2;
        int d3 = (int) Math.max(0, Math.ceil((Ws - Wr) / c));

        // LB₂ = d₁ + d₂ + d₃
        int LB2 = d1 + d2 + d3;

        // Key: If LB2 < q, means too many robots, need to reduce robot count and recalculate
        // This ensures robot resources are fully utilized, avoid overly optimistic lower bound
        if (LB2 < q && q > 0) {
            return computeLB2(remainingTasks, q - 1);
        }

        return LB2;
    }

    // ==================== Helper Methods ====================

    /**
     * Robot execution time information for a task
     */
    private static class TaskRobotInfo {
        final int task;
        final int humanTime;      // Human time tH
        final int robotOnlyTime;  // Pure robot time tR
        final int collabTime;     // Cooperative time tC
        final int robotTime;      // Actual chosen robot execution time (better of tR or tC)
        final double theta;       // Conversion coefficient
        final boolean useRobotMode;  // true=robot mode, false=cooperative mode

        TaskRobotInfo(int task, int humanTime, int robotOnlyTime, int collabTime,
                      int robotTime, double theta, boolean useRobotMode) {
            this.task = task;
            this.humanTime = humanTime;
            this.robotOnlyTime = robotOnlyTime;
            this.collabTime = collabTime;
            this.robotTime = robotTime;
            this.theta = theta;
            this.useRobotMode = useRobotMode;
        }
    }

    // ==================== Inner Classes ====================

    /**
     * Task conversion information
     *
     * <p>Used to store conversion coefficients and time information for tasks in LB₁ calculation.</p>
     */
    private static class TaskConversion {
        final int task;           // Task index
        final double theta_ir;    // Robot mode conversion coefficient
        final double theta_ic;    // Cooperative mode conversion coefficient
        final int tH, tR, tC;     // Processing time for three modes

        TaskConversion(int task, double theta_ir, double theta_ic, int tH, int tR, int tC) {
            this.task = task;
            this.theta_ir = theta_ir;
            this.theta_ic = theta_ic;
            this.tH = tH;
            this.tR = tR;
            this.tC = tC;
        }
    }
}
