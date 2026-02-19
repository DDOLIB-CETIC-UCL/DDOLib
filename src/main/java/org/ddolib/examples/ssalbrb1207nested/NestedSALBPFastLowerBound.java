package org.ddolib.examples.ssalbrb1207nested;

import org.ddolib.modeling.FastLowerBound;
import org.ddolib.examples.ssalbrb.SSALBRBProblem;

import java.util.*;

/**
 * 针对一型装配线平衡问题与人机协同资源配置的组合优化问题，
 * 实现了两种互补的下界计算方法：
 *
 *   LB₁ (工作量平衡下界):
 *   基于任务在不同执行模式间的转换系数，通过工作量平衡原理估计最少需要的工位数
 *   LB₂ (改进的装箱问题下界)：
 *   将任务按处理时间分为大、中、小三类，采用装箱策略估计最少需要的工位数。
 *   最终下界取两者的最大值：LB = max(LB₁, LB₂)
 *
 * 核心设计思想：
 *  当前工位为空时：直接计算剩余任务需要的工位数
 *  当前工位非空时：计算所有未完成任务（当前工位+剩余任务）需要的总工位数，然后减1（因为当前工位已占用1个工位）
 *
 */
public class NestedSALBPFastLowerBound implements FastLowerBound<NestedSALBPState> {

    // ==================== 问题参数 ====================

    private final int cycleTime;      // 节拍时间 c
    private final int nbTasks;        // 任务总数 n
    private final int totalRobots;    // 可用机器人总数 q

    // 任务处理时间数组（预计算以提高性能）
    private final int[] humanDur;     // 每个任务的人工时间 t_ih
    private final int[] robotDur;     // 每个任务的机器人时间 t_ir
    private final int[] collabDur;    // 每个任务的人机协同时间 t_ic

    // ==================== 构造函数 ====================

    /**
     * 构造快速下界估计器
     *
     * @param problem 嵌套SALBP问题实例
     */
    public NestedSALBPFastLowerBound(NestedSALBPProblem problem) {
        this.cycleTime = problem.cycleTime;
        this.nbTasks = problem.nbTasks;
        this.totalRobots = problem.totalRobots;

        // 预计算每个任务的处理时间，避免重复访问
        this.humanDur = new int[nbTasks];
        this.robotDur = new int[nbTasks];
        this.collabDur = new int[nbTasks];
        for (int i = 0; i < nbTasks; i++) {
            this.humanDur[i] = problem.innerProblem.humanDurations[i];
            this.robotDur[i] = problem.innerProblem.robotDurations[i];
            this.collabDur[i] = problem.innerProblem.collaborationDurations[i];
        }
    }

    // ==================== FastLowerBound 接口实现 ====================

    /**
     * 计算给定状态的快速下界
     *
     * 下界表示从当前状态到目标状态至少还需要多少个新工位。
     *
     * @param state 当前状态
     * @param variables 待决策的变量集合（本实现中未使用）
     * @return 下界值（至少还需要的新工位数）
     */
    @Override
    public double fastLowerBound(NestedSALBPState state, Set<Integer> variables) {
        // 终止条件：所有任务已分配完毕
        if (state.isComplete(nbTasks)) {
            return 0;
        }

        // 获取剩余未分配的任务和当前工位的任务
        // 🔥 使用专门的方法：下界计算时排除 maybeCompletedTasks
        Set<Integer> remainingTasks = state.getRemainingTasksForLowerBound(nbTasks);
        Set<Integer> currentStationTasks = state.currentStationTasks();

        // 特殊情况：没有剩余任务要分配
        // 此时只需关闭当前工位（如果非空），不需要额外的新工位
        if (remainingTasks.isEmpty()) {
            return 0;
        }

        // 计算剩余可用机器人数
        int remainingRobots = totalRobots - state.usedRobots();
        if (state.currentStationHasRobot()) {
            remainingRobots--;  // 当前工位的机器人也算已使用
        }

        // ========== 情况1：当前工位为空 ==========
        if (currentStationTasks.isEmpty()) {
            double lb = computeLowerBound(remainingTasks, remainingRobots);

            // 【调试】仅在root节点输出详细信息
//            if (state.completedTasks().isEmpty()) {
//                System.out.println("Root节点下界计算：");
//                System.out.println("剩余任务数: " + remainingTasks.size());
//                System.out.println("可用机器人: " + remainingRobots);
//
//                double lb1 = computeLB1(remainingTasks, remainingRobots);
//                double lb2 = computeLB2(remainingTasks, remainingRobots);
//
//                System.out.println("\n--- 下界计算结果 ---");
//                System.out.println("LB₁ (工作量下界): " + lb1);
//                System.out.println("LB₂ (装箱下界): " + lb2);
//            }

            return lb;
        }

        // ========== 情况2：当前工位非空 ==========
        // 策略：计算所有未完成任务需要的总工位数，然后减去当前工位
        Set<Integer> allUnfinishedTasks = new HashSet<>();
        allUnfinishedTasks.addAll(currentStationTasks);
        allUnfinishedTasks.addAll(remainingTasks);

        // 计算总可用机器人数（包括当前工位的机器人）
        int totalAvailableRobots = remainingRobots;
        if (state.currentStationHasRobot()) {
            totalAvailableRobots++;
        }

        double totalStationsNeeded = computeLowerBound(allUnfinishedTasks, totalAvailableRobots);

        // 减去当前工位（因为它已经存在）
        return Math.max(0, totalStationsNeeded - 1);
    }

    // ==================== 下界计算核心方法 ====================

    /**
     * 计算给定任务集合需要的最少工位数（公共方法）
     * 这个方法可以被外部调用，用于计算任意任务集合的下界
     *
     * @param tasks 任务集合
     * @param availableRobots 可用机器人数
     * @return 最少需要的工位数
     */
    public double computeLowerBound(Set<Integer> tasks, int availableRobots) {
        double lb1 = computeLB1(tasks, availableRobots);
        double lb2 = computeLB2(tasks, availableRobots);

        return Math.max(lb1, lb2);
    }

    /**
     * LB₁: 工作量平衡下界
     * 基于任务在不同执行模式间的转换系数 θ_i = min{θ_ir, θ_ic}，其中：
     *   θ_ir = t_ir / t_ih (机器人模式转换系数)
     *   θ_ic = t_ic / (t_ih - t_ic) (人机协同模式转换系数)
     * 算法思想：通过将部分任务从人工模式转换为机器人或协同模式，
     * 平衡人工工作量和机器人工作量，从而估计最少需要的工位数。
     *
     * @param remainingTasks 剩余任务集合
     * @param q 可用机器人数
     * @return LB₁下界值
     */
    private double computeLB1(Set<Integer> remainingTasks, int q) {
        if (remainingTasks.isEmpty()) {
            return 0;
        }

        // 构建任务转换信息列表
        List<TaskConversion> conversions = new ArrayList<>();
        double LH = 0.0;  // 初始人工工作量

        for (int task : remainingTasks) {
            int tH = humanDur[task];
            int tR = robotDur[task];
            int tC = collabDur[task];

            LH += tH;

            // 计算转换系数（避免除零和无效转换）
            double theta_ir = (tR < 100000 && tH > 0) ?
                    ((double) tR / tH) : Double.POSITIVE_INFINITY;
            double theta_ic = (tC < 100000 && tH > tC) ?
                    ((double) tC / (tH - tC)) : Double.POSITIVE_INFINITY;

            conversions.add(new TaskConversion(task, theta_ir, theta_ic, tH, tR, tC));
        }

        // 按转换系数升序排序（优先转换效率高的任务）
        conversions.sort(Comparator.comparingDouble(tc -> Math.min(tc.theta_ir, tc.theta_ic)));

        double LR = 0.0;  // 机器人工作量

        // 迭代转换工作量，直到达到平衡或资源耗尽
        for (TaskConversion tc : conversions) {
            // 停止条件(iii)：如果已经平衡，停止
            if (LR >= LH) {
                break;
            }

            // 记录转换前的LH和LR（用于部分转换回退）
            double LH_before = LH;
            double LR_before = LR;

            // 选择转换系数更小的模式
            if (tc.theta_ir <= tc.theta_ic) {
                // 使用机器人模式
                if (tc.tR < 100000) {
                    // 执行完全转换
                    LH -= tc.tH;
                    LR += tc.tR;

                    // 检查是否超过容量上限（停止条件ii）
                    // LR = c·q 表示所有可用机器人的总容量
                    if (LR > cycleTime * q) {
                        // 超过容量，部分转回到容量上限
                        // alpha * t_ir = (c·q - LR_before)
                        if (tc.tR > 0) {
                            double alpha = (cycleTime * q - LR_before) / tc.tR;
                            alpha = Math.max(0.0, Math.min(1.0, alpha));
                            LH = LH_before - alpha * tc.tH;
                            LR = LR_before + alpha * tc.tR;
                        }
                        break;
                    }

                    // 检查是否超过平衡点（停止条件iii）
                    if (LR > LH) {
                        // 超过平衡，部分转回到平衡点
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
                // 使用人机协同模式
                if (tc.tC < 100000) {
                    double dh = tc.tH - tc.tC;
                    double dr = tc.tC;

                    // 执行完全转换
                    LH -= dh;
                    LR += dr;

                    // 检查是否超过容量上限（停止条件ii）
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
     * LB₂: 改进的装箱问题下界
     * 核心思想：
     * 第一步：让机器人先执行任务，消耗机器人容量 c·q
     * 第二步：计算剩余任务（完全执行的移除，部分执行的需要转换时间）
     * 第三步：对剩余任务进行装箱，估计需要的工位数
     *
     * 关键点：机器人部分执行任务后，剩余工作量转换为人工时间时：
     * 机器人模式：剩余机器人时间 / 2 = 人工时间（因为 tH = tR / 2）
     * 人机协同模式：剩余工作量按比例计算人工时间
     * 装箱策略：
     *  大任务: t > c/2，各占一个箱子（工位）
     *  中等任务: c/3 < t ≤ c/2，优先填充大任务箱子的剩余容量，无法填充的成对装箱
     *  小任务: t ≤ c/3，填充所有剩余容量
     *
     * @param remainingTasks 剩余任务集合
     * @param q 可用机器人数
     * @return LB₂下界值
     */
    private double computeLB2(Set<Integer> remainingTasks, int q) {
        if (remainingTasks.isEmpty()) {
            return 0;
        }

        int c = cycleTime;

        // ========== 第一步：让机器人先执行任务 ==========
        // 机器人总容量
        double robotCapacity = c * q;

        // 按转换系数排序（与LB₁相同的顺序）
        // 优先让机器人执行转换效率高的任务
        List<TaskRobotInfo> tasksByRobotTime = new ArrayList<>();
        for (int task : remainingTasks) {
            int tH = humanDur[task];
            int tR = robotDur[task];
            int tC = collabDur[task];

            // 计算转换系数
            double theta_ir = (tR < 100000 && tH > 0) ? ((double) tR / tH) : Double.POSITIVE_INFINITY;
            double theta_ic = (tC < 100000 && tH > tC) ? ((double) tC / (tH - tC)) : Double.POSITIVE_INFINITY;
            double theta = Math.min(theta_ir, theta_ic);

            // 选择更优的机器人执行模式
            boolean useRobotMode = (theta_ir <= theta_ic);
            int robotTime = useRobotMode ? tR : tC;

            tasksByRobotTime.add(new TaskRobotInfo(task, tH, tR, tC, robotTime, theta, useRobotMode));
        }
        // 按转换系数升序排序（与LB₁一致）
        tasksByRobotTime.sort(Comparator.comparingDouble(t -> t.theta));

        // 让机器人执行任务，直到容量用完
        double usedRobotCapacity = 0.0;
        Map<Integer, Double> remainingHumanTime = new HashMap<>();

        for (TaskRobotInfo tri : tasksByRobotTime) {
            if (tri.robotTime >= 100000) {
                // 机器人不能执行这个任务
                remainingHumanTime.put(tri.task, (double) tri.humanTime);
                continue;
            }

            double availableCapacity = robotCapacity - usedRobotCapacity;

            if (availableCapacity <= 0) {
                // 机器人容量已用完，剩余任务全部由人工完成
                remainingHumanTime.put(tri.task, (double) tri.humanTime);
            } else if (tri.robotTime <= availableCapacity) {
                // 机器人可以完全执行这个任务
                usedRobotCapacity += tri.robotTime;
                // 这个任务不需要人工时间了（完全由机器人完成）
            } else {
                // 🔥 关键点：机器人只能部分执行这个任务
                // 机器人执行了 availableCapacity，剩余部分需要转换为人工时间

                double robotExecuted = availableCapacity;  // 机器人已执行的时间
                double robotRemaining = tri.robotTime - robotExecuted;  // 机器人剩余时间
                double humanTimeForRemaining;

                if (tri.useRobotMode) {
                    // 🔥 机器人模式：tH = tR / 2
                    // 剩余机器人时间转换为人工时间：robotRemaining / 2
                    humanTimeForRemaining = robotRemaining / 2.0;
                } else {
                    // 🔥 人机协同模式：剩余工作量按比例计算
                    // 机器人执行比例（完成了多少比例的工作量）
                    double robotRatio = robotExecuted / tri.robotTime;
                    // 剩余工作量比例（还剩多少比例的工作量）
                    double humanRatio = 1.0 - robotRatio;
                    // 人工剩余时间 = 原始协同时间 × 剩余比例
                    humanTimeForRemaining = tri.collabTime * humanRatio;
                }

                remainingHumanTime.put(tri.task, humanTimeForRemaining);
                usedRobotCapacity = robotCapacity; // 容量用完
            }
        }

        // ========== 第二步：对剩余任务进行装箱 ==========
        if (remainingHumanTime.isEmpty()) {
            // 所有任务都被机器人执行完了
            return 0;
        }

        // 分类任务
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

        // D₁: 大任务的箱子数
        int d1 = largeTasks.size();
        double remainingCapacityD1 = 0.0;
        for (int task : largeTasks) {
            remainingCapacityD1 += (c - remainingHumanTime.get(task));
        }

        // D₂: 中等任务的箱子数
        mediumTasks.sort(Comparator.comparingDouble(t -> remainingHumanTime.get(t)));

        // 尝试将中等任务分配到 D₁ 的剩余容量
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

        // 未分配的中等任务装箱（每个箱子最多2个任务）
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
                        // 两个任务放在一个箱子
                        d2++;
                        remainingCapacityD2 += (c - t1 - t2);
                        i += 2;
                    } else {
                        // 只放一个任务
                        d2++;
                        remainingCapacityD2 += (c - t1);
                        i++;
                    }
                } else {
                    // 最后一个任务
                    d2++;
                    remainingCapacityD2 += (c - t1);
                    i++;
                }
            }
        }

        // d₃: 小任务需要的箱子数
        double Ws = 0.0;
        for (int task : smallTasks) {
            Ws += remainingHumanTime.get(task);
        }

        double Wr = remainingCapacityD1 - usedCapacityD1 + remainingCapacityD2;
        int d3 = (int) Math.max(0, Math.ceil((Ws - Wr) / c));

        // LB₂ = d₁ + d₂ + d₃
        int LB2 = d1 + d2 + d3;

        // 关键：如果 LB2 < q，说明机器人数量过多，需要减少机器人数重新计算
        // 这确保了机器人资源被充分利用，避免下界过于乐观
        if (LB2 < q && q > 0) {
            return computeLB2(remainingTasks, q - 1);
        }

        return LB2;
    }

    // ==================== 辅助方法 ====================

    /**
     * 任务的机器人执行时间信息
     */
    private static class TaskRobotInfo {
        final int task;
        final int humanTime;      // 人工时间 tH
        final int robotOnlyTime;  // 纯机器人时间 tR
        final int collabTime;     // 人机协同时间 tC
        final int robotTime;      // 实际选择的机器人执行时间（tR或tC中较优的）
        final double theta;       // 转换系数
        final boolean useRobotMode;  // true=机器人模式，false=人机协同模式

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

    // ==================== 内部类 ====================

    /**
     * 任务转换信息
     *
     * <p>用于 LB₁ 计算中存储任务的转换系数和时间信息。</p>
     */
    private static class TaskConversion {
        final int task;           // 任务索引
        final double theta_ir;    // 机器人模式转换系数
        final double theta_ic;    // 人机协同模式转换系数
        final int tH, tR, tC;     // 三种模式的处理时间

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
