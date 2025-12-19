package org.ddolib.examples.ssalbrb1207nested;

import org.ddolib.modeling.FastLowerBound;
import org.ddolib.examples.ssalbrb.SSALBRBProblem;

import java.util.*;

/**
 * 外层问题的快速下界估计
 * 参考内层 SSALBRBFastLowerBound 的设计思路
 *
 * 内层问题：最小化单工位 makespan
 * 外层问题：最小化工位数量
 *
 * 下界策略：
 * 1. 工作量下界：总工作量 / 工位容量
 * 2. 关键路径下界：最长前置链需要的工位数
 * 3. 取最大值
 */
public class NestedSALBPFastLowerBound implements FastLowerBound<NestedSALBPState> {

    private final SSALBRBProblem innerProblem;
    private final int cycleTime;
    private final int nbTasks;
    private final int totalRobots;    // 可用机器人总数
    private final int[] minDur;       // 每个任务的最小处理时间
    private final int[] humanDur;     // 每个任务的人工时间
    private final int[] chainLength;  // 每个任务的关键路径长度（从该任务到终点）

    public NestedSALBPFastLowerBound(NestedSALBPProblem problem) {
        this.innerProblem = problem.innerProblem;
        this.cycleTime = problem.cycleTime;
        this.nbTasks = problem.nbTasks;
        this.totalRobots = problem.totalRobots;

        // 预计算每个任务的处理时间
        this.minDur = new int[nbTasks];
        this.humanDur = new int[nbTasks];
        for (int i = 0; i < nbTasks; i++) {
            int tH = innerProblem.humanDurations[i];
            int tR = innerProblem.robotDurations[i];
            int tC = innerProblem.collaborationDurations[i];
            this.minDur[i] = Math.min(tH, Math.min(tR, tC));
            this.humanDur[i] = tH;
        }

        // 预计算关键路径长度（从每个任务到终点的最长路径工作量）
        // 用于计算关键路径下界
        this.chainLength = new int[nbTasks];
        List<Integer> topo = topologicalOrder(nbTasks, innerProblem.successors);
        for (int idx = topo.size() - 1; idx >= 0; idx--) {
            int task = topo.get(idx);
            int bestSucc = 0;
            for (int succ : innerProblem.successors.getOrDefault(task, List.of())) {
                bestSucc = Math.max(bestSucc, chainLength[succ]);
            }
            chainLength[task] = minDur[task] + bestSucc;
        }
    }

    /**
     * 拓扑排序
     */
    private static List<Integer> topologicalOrder(int n, Map<Integer, List<Integer>> successors) {
        int[] indeg = new int[n];
        for (int i = 0; i < n; i++) {
            for (int succ : successors.getOrDefault(i, List.of())) {
                indeg[succ]++;
            }
        }

        ArrayList<Integer> order = new ArrayList<>(n);
        ArrayList<Integer> q = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            if (indeg[i] == 0) {
                q.add(i);
            }
        }

        for (int qi = 0; qi < q.size(); qi++) {
            int u = q.get(qi);
            order.add(u);
            for (int v : successors.getOrDefault(u, List.of())) {
                indeg[v]--;
                if (indeg[v] == 0) {
                    q.add(v);
                }
            }
        }

        return order;
    }

    @Override
    public double fastLowerBound(NestedSALBPState state, Set<Integer> variables) {
        // 下界必须 <= 实际从该状态到终点的最小代价
        // 代价 = 新开的工位数（不包括当前工位）

        if (state.isComplete(nbTasks)) {
            return 0;
        }

        // 获取剩余未分配的任务
        Set<Integer> remainingTasks = variables.isEmpty() ?
                state.getRemainingTasks(nbTasks) : variables;

        if (remainingTasks.isEmpty()) {
            return 0;
        }

        // ========== 计算剩余机器人和工位容量 ==========
        // 剩余可用机器人数
        int remainingRobots = totalRobots - state.usedRobots();
        if (state.currentStationHasRobot()) {
            // 当前工位的机器人不算在 usedRobots 中，但已经被使用
            // 实际上 usedRobots 应该包含当前工位，需要检查
        }

        // 当前工位信息
        Set<Integer> currentStationTasks = state.currentStationTasks();
        boolean currentHasRobot = state.currentStationHasRobot();

        // ========== 下界1：工作量下界 ==========
        // 使用最小处理时间计算（保证是下界）
        double sumMinDur = 0.0;
        for (int task : remainingTasks) {
            sumMinDur += minDur[task];
        }

        // 当前工位已使用的容量
        double currentStationUsed = 0.0;
        for (int task : currentStationTasks) {
            currentStationUsed += minDur[task];
        }

        // 工位容量估计：
        // - 有机器人的工位：容量 = 2 * cycleTime（乐观假设完美并行）
        // - 无机器人的工位：容量 = cycleTime
        //
        // 估计未来工位的平均容量：
        // 如果还有剩余机器人，部分工位可以有机器人
        // 为了保证是下界，我们假设所有剩余机器人都能被最优利用

        // 当前工位剩余容量
        double currentCapacity = currentHasRobot ? (2.0 * cycleTime) : cycleTime;
        double currentRemaining = Math.max(0, currentCapacity - currentStationUsed);

        // 如果剩余任务可以放入当前工位
        if (sumMinDur <= currentRemaining) {
            return 0;
        }

        // 需要分配到新工位的工作量
        double overflow = sumMinDur - currentRemaining;

        // 计算新工位数下界
        // 假设剩余的 remainingRobots 个机器人可以分配给新工位
        // 每个有机器人的工位容量 = 2 * cycleTime
        // 每个无机器人的工位容量 = cycleTime
        //
        // 为了得到下界，我们假设优先使用机器人工位（容量更大）
        double lbWorkload = 0;
        double remainingWork = overflow;
        int robotsLeft = remainingRobots;

        while (remainingWork > 0) {
            if (robotsLeft > 0) {
                // 使用一个有机器人的工位
                remainingWork -= 2.0 * cycleTime;
                robotsLeft--;
            } else {
                // 使用无机器人的工位
                remainingWork -= cycleTime;
            }
            lbWorkload++;
        }

        // ========== 下界2：关键路径下界 ==========
        // 找剩余任务中最长的关键路径
        double maxChain = 0.0;
        for (int task : remainingTasks) {
            maxChain = Math.max(maxChain, chainLength[task]);
        }

        // 关键路径需要的总工位数
        // 使用乐观估计：2 * cycleTime
        double lbCriticalPath = Math.ceil(maxChain / (2.0 * cycleTime));

        // 减去当前工位可以贡献的容量
        if (!currentStationTasks.isEmpty() && lbCriticalPath > 0) {
            lbCriticalPath = Math.max(0, lbCriticalPath - 1);
        }

        // ========== 返回最大下界 ==========
        return Math.max(0, Math.max(lbWorkload, lbCriticalPath));
    }
}
