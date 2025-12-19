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

        // ========== 下界1：转换率(Cr)工作量平衡下界 ==========
        // 参考论文中的方法：
        // 1. 计算 Cr_i = min(t_iR/t_iH, t_iC/(t_iH - t_iC))
        // 2. 按 Cr_i 排序，将工作量从 human 转移到 robot
        // 3. 计算平衡后的 LH
        // 4. 工位数下界 = ceil(LH / cycleTime)

        // 收集剩余任务和当前工位任务的转换信息
        List<double[]> candidates = new ArrayList<>();  // [task, mode, cr, tH, tR, tC]
        double lh = 0.0;  // 初始 LH = 所有任务的人工时间之和
        double maxMinDur = 0.0;  // max{min_p t_ip}

        // 处理剩余任务
        for (int task : remainingTasks) {
            int tH = innerProblem.humanDurations[task];
            int tR = innerProblem.robotDurations[task];
            int tC = innerProblem.collaborationDurations[task];

            lh += tH;
            maxMinDur = Math.max(maxMinDur, minDur[task]);

            // Cr_i^R = t_iR / t_iH
            double crR = tH > 0 ? ((double) tR) / tH : Double.POSITIVE_INFINITY;
            // Cr_i^C = t_iC / (t_iH - t_iC)
            double crC = tH > tC ? ((double) tC) / (tH - tC) : Double.POSITIVE_INFINITY;

            // 选择转换率更小的模式
            if (crR <= crC) {
                candidates.add(new double[]{task, 0, crR, tH, tR, tC});  // mode=0: robot
            } else {
                candidates.add(new double[]{task, 1, crC, tH, tR, tC});  // mode=1: collab
            }
        }

        // 处理当前工位任务
        for (int task : currentStationTasks) {
            int tH = innerProblem.humanDurations[task];
            int tR = innerProblem.robotDurations[task];
            int tC = innerProblem.collaborationDurations[task];

            lh += tH;
            maxMinDur = Math.max(maxMinDur, minDur[task]);

            double crR = tH > 0 ? ((double) tR) / tH : Double.POSITIVE_INFINITY;
            double crC = tH > tC ? ((double) tC) / (tH - tC) : Double.POSITIVE_INFINITY;

            if (crR <= crC) {
                candidates.add(new double[]{task, 0, crR, tH, tR, tC});
            } else {
                candidates.add(new double[]{task, 1, crC, tH, tR, tC});
            }
        }

        // 按 Cr 升序排序
        candidates.sort(Comparator.comparingDouble(c -> c[2]));

        // 计算可用机器人总数（当前工位 + 剩余）
        int totalAvailableRobots = remainingRobots + (currentHasRobot ? 1 : 0);

        // m = 工位数（我们要求的），q = 机器人数
        // 假设所有机器人都被使用，这是乐观估计
        int q = totalAvailableRobots;

        // 迭代转换工作量，直到 LR * 1 >= LH * q/m
        // 简化：我们用固定的 q 来计算平衡点
        // 由于我们不知道 m，我们假设 m 足够大，让所有机器人都能被分配
        // 目标：LR * m = LH * q，即 LR/q = LH/m
        // 最终 LC = LH/m = LR/q

        double lr = 0.0;
        double lhBefore = lh;
        double lrBefore = lr;
        int crossingIdx = -1;

        for (int i = 0; i < candidates.size() && q > 0; i++) {
            double[] cand = candidates.get(i);
            int mode = (int) cand[1];
            int tH = (int) cand[3];
            int tR = (int) cand[4];
            int tC = (int) cand[5];

            lhBefore = lh;
            lrBefore = lr;

            // 更新 LH 和 LR
            if (mode == 0) {  // robot
                lh -= tH;
                lr += tR;
            } else {  // collaboration
                lh -= (tH - tC);
                lr += tC;
            }

            // 检查是否超过平衡点 (LR >= LH 即 q=m 时的平衡)
            // 更一般的平衡条件是 LR/q = LH/m
            // 为简化，我们用 LR >= LH * (q / m_est) 来估计
            // 假设 m_est = max(1, 工位数估计)
            if (lr >= lh) {  // 简化的平衡条件
                crossingIdx = i;
                break;
            }
        }

        // 线性插值修正（如果超过了平衡点）
        if (crossingIdx >= 0 && lrBefore < lhBefore) {
            double[] cand = candidates.get(crossingIdx);
            int mode = (int) cand[1];
            int tH = (int) cand[3];
            int tR = (int) cand[4];
            int tC = (int) cand[5];

            // 使用线性插值找到精确的平衡点
            // 目标：LR' = LH' (q=m 时)
            if (mode == 0) {  // robot
                double denom = (double) tR + (double) tH;
                if (denom > 0) {
                    double alpha = (lhBefore - lrBefore) / denom;
                    alpha = Math.max(0, Math.min(1, alpha));
                    lh = lhBefore - alpha * tH;
                    lr = lrBefore + alpha * tR;
                }
            } else {  // collaboration
                double dh = tH - tC;
                double dr = tC;
                double denom = dr + dh;
                if (denom > 0) {
                    double alpha = (lhBefore - lrBefore) / denom;
                    alpha = Math.max(0, Math.min(1, alpha));
                    lh = lhBefore - alpha * dh;
                    lr = lrBefore + alpha * dr;
                }
            }
        }

        // LC = max{LH/m, max{min_p t_ip}}
        // 对于最小化工位数：给定 cycleTime，m >= LH / cycleTime
        // 考虑 maxMinDur：如果 maxMinDur > cycleTime，问题无解
        // 否则，工位数下界 = ceil(LH / cycleTime)
        double lc = Math.max(lh, maxMinDur);  // LH 已经是平衡后的工作量

        // 当前工位已用容量
        double currentStationUsed = 0.0;
        for (int task : currentStationTasks) {
            currentStationUsed += minDur[task];
        }

        // 当前工位剩余容量
        double currentCapacity = currentHasRobot ? (2.0 * cycleTime) : cycleTime;
        double currentRemaining = Math.max(0, currentCapacity - currentStationUsed);

        // 如果剩余工作量可以放入当前工位
        if (lc <= currentRemaining) {
            return 0;
        }

        // 需要分配到新工位的工作量
        double overflow = lc - currentRemaining;

        // 计算新工位数下界
        // 假设剩余的 remainingRobots 个机器人可以分配给新工位
        double lbWorkload = 0;
        double remainingWork = overflow;
        int robotsLeft = remainingRobots;

        while (remainingWork > 0) {
            if (robotsLeft > 0) {
                remainingWork -= 2.0 * cycleTime;
                robotsLeft--;
            } else {
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

        // ========== 下界3：大任务下界 (3B) ==========
        // 如果一个任务的 minDur > cycleTime/2，称为“大任务”
        // 两个大任务不能放在同一个工位（即使有机器人，容量也只有 2*cycleTime）
        // 所以 n 个大任务至少需要 ceil(n/2) 个工位
        int bigTaskCount = 0;
        double halfCapacity = cycleTime;  // 有机器人工位容量的一半
        for (int task : remainingTasks) {
            if (minDur[task] > halfCapacity) {
                bigTaskCount++;
            }
        }

        // 检查当前工位是否已经有大任务
        boolean currentHasBigTask = false;
        for (int task : currentStationTasks) {
            if (minDur[task] > halfCapacity) {
                currentHasBigTask = true;
                break;
            }
        }

        // 计算大任务下界
        double lbBigTask = 0;
        if (bigTaskCount > 0) {
            if (currentHasBigTask) {
                // 当前工位已有大任务，最多再放一个大任务
                // 剩余大任务需要新工位
                int remainingBig = Math.max(0, bigTaskCount - 1);  // 当前工位可以再放1个
                lbBigTask = Math.ceil(remainingBig / 2.0);
            } else {
                // 当前工位没有大任务，可以放2个大任务
                int remainingBig = Math.max(0, bigTaskCount - 2);
                lbBigTask = Math.ceil(remainingBig / 2.0);
            }
        }

        // ========== 返回最大下界 ==========
        return Math.max(0, Math.max(lbWorkload, Math.max(lbCriticalPath, lbBigTask)));
    }
}
