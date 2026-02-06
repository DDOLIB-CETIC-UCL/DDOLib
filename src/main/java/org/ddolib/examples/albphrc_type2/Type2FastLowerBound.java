package org.ddolib.examples.albphrc_type2;

import org.ddolib.modeling.FastLowerBound;

import java.util.*;

/**
 * 二型 ALBP-HRC 的快速下界估计
 * 
 * 下界策略：
 * 1. 工作量下界：总工作量 / 工位数
 * 2. 使用转换率方法平衡人工和机器人工作量
 * 
 * 与一型的区别：
 * - 一型：LB = ⌈总工作量 / 节拍时间⌉ (最小化工位数)
 * - 二型：LB = ⌈总工作量 / 工位数⌉ (最小化节拍时间)
 */
public class Type2FastLowerBound implements FastLowerBound<Type2State> {
    
    private final Type2Problem problem;
    private final int maxStations;
    private final int totalRobots;
    private final int nbTasks;
    private final int[] minDur;       // 每个任务的最小处理时间
    
    private static long lbCallCount = 0;
    private static long lastLogTime = System.currentTimeMillis();
    
    public Type2FastLowerBound(Type2Problem problem) {
        this.problem = problem;
        this.maxStations = problem.maxStations;
        this.totalRobots = problem.totalRobots;
        this.nbTasks = problem.nbTasks;
        
        // 预计算每个任务的最小处理时间
        this.minDur = new int[nbTasks];
        for (int i = 0; i < nbTasks; i++) {
            int tH = problem.humanDurations[i];
            int tR = problem.robotDurations[i];
            int tC = problem.collaborationDurations[i];
            this.minDur[i] = Math.min(tH, Math.min(tR, tC));
        }
    }
    
    @Override
    public double fastLowerBound(Type2State state, Set<Integer> variables) {
        // 下界必须 <= 实际从该状态到终点的最小代价
        // 代价 = maxLoadSoFar 的增量
        
        if (state.isComplete(nbTasks)) {
            return 0;
        }
        
        lbCallCount++;
        
        // 获取剩余未分配的任务
        Set<Integer> remainingTasks = variables.isEmpty() ?
                state.getRemainingTasks(nbTasks) : variables;
        
        if (remainingTasks.isEmpty()) {
            return 0;
        }
        
        // ========== 计算剩余机器人和工位容量 ==========
        int remainingRobots = state.remainingRobots(totalRobots);
        int remainingStations = maxStations - state.stationIndex();
        
        if (remainingStations <= 0) {
            // 没有剩余工位，必须全部放入当前工位
            // 这种情况下，下界 = 当前工位负载 + 剩余任务的最小时间和
            int remainingWork = 0;
            for (int task : remainingTasks) {
                remainingWork += minDur[task];
            }
            int newLoad = state.currentStationLoad() + remainingWork;
            return Math.max(0, newLoad - state.maxLoadSoFar());
        }
        
        // ========== 下界1：转换率(Cr)工作量平衡下界 ==========
        // 参考一型问题的方法，但目标不同：
        // 一型：LB = ⌈LH / C⌉ (工位数)
        // 二型：LB = ⌈LH / m⌉ (节拍时间)
        
        List<double[]> candidates = new ArrayList<>();  // [task, mode, cr, tH, tR, tC]
        double lh = 0.0;  // 初始 LH = 所有任务的人工时间之和
        double maxMinDur = 0.0;  // max{min_p t_ip}
        
        // 处理剩余任务
        for (int task : remainingTasks) {
            int tH = problem.humanDurations[task];
            int tR = problem.robotDurations[task];
            int tC = problem.collaborationDurations[task];
            
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
        
        // 按 Cr 升序排序
        candidates.sort(Comparator.comparingDouble(c -> c[2]));
        
        // 计算可用机器人总数（当前工位 + 剩余）
        int totalAvailableRobots = remainingRobots;
        if (state.currentStationHasRobot() && state.stationIndex() > 0) {
            totalAvailableRobots++;
        }
        
        // 迭代转换工作量，直到 LR >= LH
        double lr = 0.0;
        int q = totalAvailableRobots;
        
        for (int i = 0; i < candidates.size() && q > 0; i++) {
            double[] cand = candidates.get(i);
            int mode = (int) cand[1];
            int tH = (int) cand[3];
            int tR = (int) cand[4];
            int tC = (int) cand[5];
            
            // 更新 LH 和 LR
            if (mode == 0) {  // robot
                lh -= tH;
                lr += tR;
            } else {  // collaboration
                lh -= (tH - tC);
                lr += tC;
            }
            
            // 检查是否超过平衡点
            if (lr >= lh) {
                break;
            }
        }
        
        // ========== 计算下界 ==========
        // 二型问题：给定 m 个工位，最小化节拍时间
        // 下界 = ⌈平衡后的工作量 / 可用工位数⌉
        
        // 可用工位数 = 剩余工位数 + 当前工位（如果还有容量）
        int availableStations = remainingStations;
        if (state.stationIndex() > 0) {
            availableStations++;  // 当前工位也可以使用
        }
        
        // 平衡后的工作量
        double balancedWorkload = Math.max(lh, maxMinDur);
        
        // 当前工位已用容量
        int currentUsed = state.currentStationLoad();
        
        // 如果当前工位还有任务，需要考虑其负载
        double totalWorkload = balancedWorkload;
        if (state.stationIndex() > 0 && currentUsed > 0) {
            // 当前工位的负载已经在 maxLoadSoFar 中体现
            // 我们需要计算：如果把剩余任务分配到 availableStations 个工位
            // 每个工位的平均负载是多少
        }
        
        // 下界 = 平均负载
        double avgLoad = totalWorkload / availableStations;
        
        // 考虑当前工位的负载
        double lowerBound = Math.max(avgLoad, maxMinDur);
        
        // 如果当前工位已有负载，需要考虑
        if (state.stationIndex() > 0) {
            lowerBound = Math.max(lowerBound, currentUsed);
        }
        
        // 下界增量 = max(lowerBound, maxLoadSoFar) - maxLoadSoFar
        double lbIncrement = Math.max(0, lowerBound - state.maxLoadSoFar());
        
        // 周期性打印
        long now = System.currentTimeMillis();
        if (lbCallCount == 1 || now - lastLogTime > 5000) {
            System.out.printf("[Type2-LB] calls=%d, remaining=%d, avgLoad=%.2f, LB=%.2f%n",
                             lbCallCount, remainingTasks.size(), avgLoad, lbIncrement);
            lastLogTime = now;
        }
        
        return lbIncrement;
    }
}
