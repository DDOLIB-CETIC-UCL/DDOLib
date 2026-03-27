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
     * 第一步：修正任务列表
     * 第二步：装箱计算
     *
     * @param remainingTasks 剩余任务集合
     * @param q 可用机器人数
     * @return LB₂下界值
     */
    private double computeLB2(Set<Integer> remainingTasks, int q) {
        if (remainingTasks.isEmpty()) {
            return 0;
        }

        int currentQ = q;
        double lb2;

        do {
            // 第一步：修正任务列表
            List<ModifiedTask> modifiedTasks = modifyTaskList(remainingTasks, currentQ);

            // 第二步：装箱计算
            lb2 = binPacking(modifiedTasks);

            // 如果 LB2 < q，减少机器人数量并重新计算
            if (lb2 < currentQ) {
                currentQ = currentQ - 1;
            } else {
                break;
            }
        } while (true);

        return lb2;
    }

    /**
     * 修正任务列表：根据机器人容量和任务操作模式修正任务
     * 新思路：优先将大任务转化为中任务，再将中任务转化为小任务
     *
     * @param remainingTasks 剩余任务集合
     * @param q 可用机器人数
     * @return 修正后的任务列表
     */
    private List<ModifiedTask> modifyTaskList(Set<Integer> remainingTasks, int q) {
        double robotCapacity = cycleTime * q;  // 机器人总容量

        // 初始化任务列表（所有任务保持人工模式）
        Map<Integer, Double> taskTimes = new HashMap<>();
        for (int task : remainingTasks) {
            taskTimes.put(task, (double) humanDur[task]);
        }

        double mediumThreshold = cycleTime / 2.0;
        double smallThreshold = cycleTime / 3.0;

        // ===== 阶段1：将大任务转化为中任务 =====
        robotCapacity = convertLargeToMedium(taskTimes, robotCapacity, mediumThreshold);

        // ===== 阶段2：将中任务转化为小任务 =====
        robotCapacity = convertMediumToSmall(taskTimes, robotCapacity, mediumThreshold, smallThreshold);

        // ===== 阶段3：消耗剩余机器人容量处理小任务 =====
        if (robotCapacity > 0) {
            consumeRemainingCapacity(taskTimes, robotCapacity, smallThreshold);
        }

        // 转换为ModifiedTask列表
        List<ModifiedTask> modifiedTasks = new ArrayList<>();
        for (Map.Entry<Integer, Double> entry : taskTimes.entrySet()) {
            modifiedTasks.add(new ModifiedTask(entry.getKey(), entry.getValue()));
        }

        return modifiedTasks;
    }

    /**
     * 阶段1：将大任务转化为中任务
     */
    private double convertLargeToMedium(Map<Integer, Double> taskTimes, double robotCapacity, double mediumThreshold) {
        List<TaskConversionCost> conversionCosts = new ArrayList<>();

        for (Map.Entry<Integer, Double> entry : taskTimes.entrySet()) {
            int task = entry.getKey();
            double currentTime = entry.getValue();

            // 只处理大任务
            if (currentTime <= mediumThreshold) continue;

            double targetTime = mediumThreshold;
            double humanTimeToReduce = currentTime - targetTime;

            int tH = humanDur[task];
            int tR = robotDur[task];
            int tC = collabDur[task];

            // 计算转化所需的机器人时间
            double robotTimeNeeded = Double.POSITIVE_INFINITY;
            boolean useRobotMode = false;

            // 机器人模式
            if (tR < 100000) {
                double robotTime = humanTimeToReduce / tH * tR;
                if (robotTime < robotTimeNeeded) {
                    robotTimeNeeded = robotTime;
                    useRobotMode = true;
                }
            }

            // 人机协同模式（协作时间必须 <= mediumThreshold）
            if (tC < 100000 && tC <= mediumThreshold) {
                double robotTime = humanTimeToReduce / (tH - tC) * tC;
                if (robotTime < robotTimeNeeded) {
                    robotTimeNeeded = robotTime;
                    useRobotMode = false;
                }
            }

            if (robotTimeNeeded < Double.POSITIVE_INFINITY) {
                conversionCosts.add(new TaskConversionCost(task, robotTimeNeeded, targetTime, useRobotMode));
            }
        }

        // 按消耗机器人时间升序排序
        conversionCosts.sort(Comparator.comparingDouble(c -> c.robotTimeNeeded));

        // 依次转化
        for (TaskConversionCost cost : conversionCosts) {
            if (robotCapacity >= cost.robotTimeNeeded) {
                taskTimes.put(cost.task, cost.targetTime);
                robotCapacity -= cost.robotTimeNeeded;
            } else {
                break;
            }
        }

        return robotCapacity;
    }

    /**
     * 阶段2：将中任务转化为小任务
     */
    private double convertMediumToSmall(Map<Integer, Double> taskTimes, double robotCapacity,
                                        double mediumThreshold, double smallThreshold) {
        List<TaskConversionCost> conversionCosts = new ArrayList<>();

        for (Map.Entry<Integer, Double> entry : taskTimes.entrySet()) {
            int task = entry.getKey();
            double currentTime = entry.getValue();

            // 只处理中任务
            if (currentTime <= smallThreshold || currentTime > mediumThreshold) continue;

            double targetTime = smallThreshold;
            double humanTimeToReduce = currentTime - targetTime;

            int tH = humanDur[task];
            int tR = robotDur[task];
            int tC = collabDur[task];

            // 计算转化所需的机器人时间
            double robotTimeNeeded = Double.POSITIVE_INFINITY;
            boolean useRobotMode = false;

            // 机器人模式
            if (tR < 100000) {
                double robotTime = humanTimeToReduce / tH * tR;
                if (robotTime < robotTimeNeeded) {
                    robotTimeNeeded = robotTime;
                    useRobotMode = true;
                }
            }

            // 人机协同模式（协作时间必须 <= smallThreshold）
            if (tC < 100000 && tC <= smallThreshold) {
                double robotTime = humanTimeToReduce / (tH - tC) * tC;
                if (robotTime < robotTimeNeeded) {
                    robotTimeNeeded = robotTime;
                    useRobotMode = false;
                }
            }

            if (robotTimeNeeded < Double.POSITIVE_INFINITY) {
                conversionCosts.add(new TaskConversionCost(task, robotTimeNeeded, targetTime, useRobotMode));
            }
        }

        // 按消耗机器人时间升序排序
        conversionCosts.sort(Comparator.comparingDouble(c -> c.robotTimeNeeded));

        // 依次转化
        for (TaskConversionCost cost : conversionCosts) {
            if (robotCapacity >= cost.robotTimeNeeded) {
                taskTimes.put(cost.task, cost.targetTime);
                robotCapacity -= cost.robotTimeNeeded;
            } else {
                break;
            }
        }

        return robotCapacity;
    }

    /**
     * 阶段3：消耗剩余机器人容量处理小任务
     */
    private void consumeRemainingCapacity(Map<Integer, Double> taskTimes, double robotCapacity, double smallThreshold) {
        // 收集所有小任务
        List<TaskConversionRate> smallTasks = new ArrayList<>();

        for (Map.Entry<Integer, Double> entry : taskTimes.entrySet()) {
            int task = entry.getKey();
            double currentTime = entry.getValue();

            // 只处理小任务
            if (currentTime > smallThreshold) continue;

            int tH = humanDur[task];
            int tR = robotDur[task];
            int tC = collabDur[task];

            // 计算转化率
            double theta_ir = (tR < 100000 && tH > 0) ? ((double) tR / tH) : Double.POSITIVE_INFINITY;
            double theta_ic = (tC < 100000 && tH > tC) ? ((double) tC / (tH - tC)) : Double.POSITIVE_INFINITY;

            double minTheta = Math.min(theta_ir, theta_ic);
            boolean useRobotMode = (theta_ir <= theta_ic);

            smallTasks.add(new TaskConversionRate(task, minTheta, useRobotMode, tH, tR, tC));
        }

        // 按转化率升序排序
        smallTasks.sort(Comparator.comparingDouble(t -> t.theta));

        // 消耗剩余机器人容量
        for (TaskConversionRate taskInfo : smallTasks) {
            if (robotCapacity <= 0) break;

            double currentTime = taskTimes.get(taskInfo.task);

            if (taskInfo.useRobotMode && taskInfo.tR < 100000) {
                // 机器人模式
                if (robotCapacity >= taskInfo.tR) {
                    // 完全转化，任务被删除
                    taskTimes.remove(taskInfo.task);
                    robotCapacity -= taskInfo.tR;
                } else {
                    // 部分转化
                    double newTime = taskInfo.tH * (1 - robotCapacity / taskInfo.tR);
                    taskTimes.put(taskInfo.task, newTime);
                    robotCapacity = 0;
                }
            } else if (!taskInfo.useRobotMode && taskInfo.tC < 100000) {
                // 人机协同模式
                if (robotCapacity >= taskInfo.tC) {
                    // 完全转化
                    taskTimes.put(taskInfo.task, (double) taskInfo.tC);
                    robotCapacity -= taskInfo.tC;
                } else {
                    // 部分转化
                    double newTime = taskInfo.tH - (taskInfo.tH - taskInfo.tC) / taskInfo.tC * robotCapacity;
                    taskTimes.put(taskInfo.task, newTime);
                    robotCapacity = 0;
                }
            }
        }
    }

    /**
     * 装箱计算：将修正后的任务按大中小分类并装箱
     *
     * @param modifiedTasks 修正后的任务列表
     * @return 需要的箱子数（工位数）
     */
    private double binPacking(List<ModifiedTask> modifiedTasks) {
        if (modifiedTasks.isEmpty()) {
            return 0;
        }

        // 分类：大、中、小任务
        List<ModifiedTask> largeTasks = new ArrayList<>();
        List<ModifiedTask> mediumTasks = new ArrayList<>();
        List<ModifiedTask> smallTasks = new ArrayList<>();

        for (ModifiedTask task : modifiedTasks) {
            if (task.modifiedTime > cycleTime / 2.0) {
                largeTasks.add(task);
            } else if (task.modifiedTime > cycleTime / 3.0) {
                mediumTasks.add(task);
            } else {
                smallTasks.add(task);
            }
        }


        // D1: 大任务，每个占一个箱子
        int d1 = largeTasks.size();
        List<Double> D1_remainingCapacity = new ArrayList<>();
        for (ModifiedTask largeTask : largeTasks) {
            D1_remainingCapacity.add(cycleTime - largeTask.modifiedTime);
        }

        // 中任务按时间升序排序（小→大）
        mediumTasks.sort(Comparator.comparingDouble(t -> t.modifiedTime));

        // 大箱子剩余容量按升序排序（小→大）
        D1_remainingCapacity.sort(Comparator.naturalOrder());

        // 双指针贪心：最小中任务 → 最小剩余容量的大箱子
        List<ModifiedTask> unassignedMediumTasks = new ArrayList<>();
        int binIndex = 0;  // 当前考虑的大箱子索引

        for (ModifiedTask mediumTask : mediumTasks) {
            boolean assigned = false;

            // 从当前箱子开始，找第一个能装下的箱子
            while (binIndex < D1_remainingCapacity.size()) {
                if (D1_remainingCapacity.get(binIndex) >= mediumTask.modifiedTime) {
                    // 找到能装下的箱子，分配
                    D1_remainingCapacity.set(binIndex, D1_remainingCapacity.get(binIndex) - mediumTask.modifiedTime);
                    assigned = true;
                    binIndex++;  // 下一个中任务从下一个箱子开始尝试
                    break;
                } else {
                    // 当前箱子装不下，跳过（它也装不下后面更大的中任务）
                    binIndex++;
                }
            }

            if (!assigned) {
                // 所有箱子都装不下
                unassignedMediumTasks.add(mediumTask);
            }
        }

        // D2: 未分配的中任务，每个箱子最多放2个
        int f = unassignedMediumTasks.size();
        int d2 = (int) Math.ceil(f / 2.0);

        // 计算大任务和中任务的总时间
        double totalLargeMediumTime = 0;
        for (ModifiedTask largeTask : largeTasks) {
            totalLargeMediumTime += largeTask.modifiedTime;
        }
        for (ModifiedTask mediumTask : mediumTasks) {
            totalLargeMediumTime += mediumTask.modifiedTime;
        }

        // 计算D1和D2的总剩余容量
        double Wr = cycleTime * (d1 + d2) - totalLargeMediumTime;

        // 小任务：计算总工作量
        double Ws = 0;
        for (ModifiedTask smallTask : smallTasks) {
            Ws += smallTask.modifiedTime;
        }

        // D3: 小任务需要的箱子数
        int d3 = (int) Math.max(0, Math.ceil((Ws - Wr) / cycleTime));

        // LB2 = d1 + d2 + d3
        return d1 + d2 + d3;
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

    /**
     * 修正后的任务信息
     *
     * <p>用于 LB₂ 计算中存储修正后的任务时间。</p>
     */
    private static class ModifiedTask {
        final int task;              // 任务索引
        final double modifiedTime;   // 修正后的时间

        ModifiedTask(int task, double modifiedTime) {
            this.task = task;
            this.modifiedTime = modifiedTime;
        }
    }

    /**
     * 任务转化成本信息
     */
    private static class TaskConversionCost {
        final int task;
        final double robotTimeNeeded;
        final double targetTime;
        final boolean useRobotMode;

        TaskConversionCost(int task, double robotTimeNeeded, double targetTime, boolean useRobotMode) {
            this.task = task;
            this.robotTimeNeeded = robotTimeNeeded;
            this.targetTime = targetTime;
            this.useRobotMode = useRobotMode;
        }
    }

    /**
     * 任务转化率信息
     */
    private static class TaskConversionRate {
        final int task;
        final double theta;
        final boolean useRobotMode;
        final int tH, tR, tC;

        TaskConversionRate(int task, double theta, boolean useRobotMode, int tH, int tR, int tC) {
            this.task = task;
            this.theta = theta;
            this.useRobotMode = useRobotMode;
            this.tH = tH;
            this.tR = tR;
            this.tC = tC;
        }
    }
}
