package org.ddolib.examples.ssalbrb1207nested;

import java.util.HashSet;
import java.util.Set;

/**
 * 嵌套SALBP状态（支持机器人分配决策）- 支持Relax的版本
 *
 * 状态表示：
 * - completedTasks: 确定已完成工位中的任务（Relax时取交集）
 * - currentStationTasks: 确定在当前工位的任务（Relax时取交集）
 * - maybeCompletedTasks: 可能已完成或在当前工位的任务（Relax时取并集-交集）
 * - currentStationHasRobot: 当前工位是否有机器人
 * - usedRobots: 已使用的机器人数量（包括已完成工位，不包括当前工位）
 *
 * maybeCompletedTasks 的作用：
 * - 只在Relax合并时产生
 * - 在transition时可能减少（当分配了其中的任务）
 * - 用于下界计算的乐观估计
 */
public record NestedSALBPState(
        Set<Integer> completedTasks,                // 确定已完成的任务
        Set<Integer> currentStationTasks,           // 确定在当前工位的任务
        Set<Integer> maybeCompletedTasks,           // 可能已完成的任务
        boolean currentStationHasRobot,             // 当前工位是否有机器人
        int usedRobots) {                           // 已使用的机器人数

    public NestedSALBPState {
        // 防御性复制
        completedTasks = Set.copyOf(completedTasks);
        currentStationTasks = Set.copyOf(currentStationTasks);
        maybeCompletedTasks = Set.copyOf(maybeCompletedTasks);
        
        // 不变式检查：三个集合应该互不相交
        Set<Integer> intersection1 = new HashSet<>(completedTasks);
        intersection1.retainAll(currentStationTasks);
        if (!intersection1.isEmpty()) {
            throw new IllegalArgumentException(
                "completedTasks and currentStationTasks must be disjoint");
        }
        
        Set<Integer> intersection2 = new HashSet<>(completedTasks);
        intersection2.retainAll(maybeCompletedTasks);
        if (!intersection2.isEmpty()) {
            throw new IllegalArgumentException(
                "completedTasks and maybeCompletedTasks must be disjoint");
        }
        
        Set<Integer> intersection3 = new HashSet<>(currentStationTasks);
        intersection3.retainAll(maybeCompletedTasks);
        if (!intersection3.isEmpty()) {
            throw new IllegalArgumentException(
                "currentStationTasks and maybeCompletedTasks must be disjoint");
        }
    }

    /**
     * 检查是否所有任务都已分配
     * 
     * 🔥 关键：不包括 maybeCompletedTasks
     * 因为这些任务可能还需要被重新分配
     */
    public boolean isComplete(int totalTasks) {
        return completedTasks.size() + currentStationTasks.size() == totalTasks;
    }

    /**
     * 获取剩余任务集合（用于 domain 生成决策）
     * 
     * 🔥 关键：maybeCompletedTasks 中的任务仍然在 remaining 中！
     * 因为这些任务可能还没有被完成，需要继续分配
     */
    public Set<Integer> getRemainingTasks(int totalTasks) {
        java.util.Set<Integer> remaining = new java.util.LinkedHashSet<>();
        for (int i = 0; i < totalTasks; i++) {
            remaining.add(i);
        }
        // 只移除确定已完成的和确定在当前工位的任务
        remaining.removeAll(completedTasks);
        remaining.removeAll(currentStationTasks);
        // 🔥 不移除 maybeCompletedTasks（这些任务可能还需要分配）
        return remaining;
    }

    /**
     * 获取剩余任务集合（用于下界计算）
     * 
     * 🔥 关键：maybeCompletedTasks 中的任务不在这个集合中！
     * 因为下界计算时，我们乐观地假设这些任务已经被高效地完成了
     */
    public Set<Integer> getRemainingTasksForLowerBound(int totalTasks) {
        java.util.Set<Integer> remaining = new java.util.LinkedHashSet<>();
        for (int i = 0; i < totalTasks; i++) {
            remaining.add(i);
        }
        // 移除所有已分配的任务（包括 maybe）
        remaining.removeAll(completedTasks);
        remaining.removeAll(currentStationTasks);
        remaining.removeAll(maybeCompletedTasks);  // 🔥 下界计算时移除 maybe
        return remaining;
    }

    /**
     * 剩余机器人数（需要传入总机器人数）
     */
    public int remainingRobots(int totalRobots) {
        int currentUsed = currentStationHasRobot() ? 1 : 0;
        return Math.max(0, totalRobots - usedRobots() - currentUsed);
    }

    @Override
    public String toString() {
        return String.format("<Completed=%d, CS=%s, Maybe=%d, CSRobot=%s, usedRobots=%d>",
                completedTasks.size(), currentStationTasks,
                maybeCompletedTasks.size(),
                currentStationHasRobot ? "Y" : "N",
                usedRobots);
    }
}
