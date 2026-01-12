package org.ddolib.examples.ssalbrb1207nested;

import java.util.Set;

/**
 * 嵌套SALBP状态（支持机器人分配决策）- 简化版1层结构
 *
 * 状态表示：
 * - completedTasks: 已完成工位中的所有任务（不区分工位）
 * - currentStationTasks: 当前工位已分配的任务集合
 * - currentStationHasRobot: 当前工位是否有机器人
 * - usedRobots: 已使用的机器人数量（包括已完成工位，不包括当前工位）
 *
 * 推导信息：
 * - remainingTasks = allTasks \ (completedTasks ∪ currentStationTasks)
 * - remainingRobots = totalRobots - usedRobots - (currentStationHasRobot ? 1 : 0)
 */
public record NestedSALBPState(
        Set<Integer> completedTasks,                // 已完成工位中的所有任务
        Set<Integer> currentStationTasks,           // 当前工位的任务
        boolean currentStationHasRobot,             // 当前工位是否有机器人
        int usedRobots) {                           // 已使用的机器人数

    public NestedSALBPState {
        // 防御性复制
        completedTasks = Set.copyOf(completedTasks);
        currentStationTasks = Set.copyOf(currentStationTasks);
    }

    /**
     * 检查是否所有任务都已分配
     */
    public boolean isComplete(int totalTasks) {
        return completedTasks.size() + currentStationTasks.size() == totalTasks;
    }

    /**
     * 获取剩余任务集合
     */
    public Set<Integer> getRemainingTasks(int totalTasks) {
        java.util.Set<Integer> remaining = new java.util.LinkedHashSet<>();
        for (int i = 0; i < totalTasks; i++) {
            remaining.add(i);
        }
        // 移除已分配的任务
        remaining.removeAll(completedTasks);
        remaining.removeAll(currentStationTasks);
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
        return String.format("<CompletedTasks=%d, CS=%s, CSRobot=%s, usedRobots=%d>",
                completedTasks.size(), currentStationTasks,
                currentStationHasRobot ? "Y" : "N",
                usedRobots);
    }
}
