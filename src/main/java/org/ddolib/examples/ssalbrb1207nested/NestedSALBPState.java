package org.ddolib.examples.ssalbrb1207nested;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * 嵌套SALBP状态（支持机器人分配决策）- 简化为4字段
 * 
 * 状态表示：
 * - completedStations: 已完成的工位列表（每个工位的任务集合）
 * - currentStationTasks: 当前工位已分配的任务集合
 * - currentStationHasRobot: 当前工位是否有机器人
 * - usedRobots: 已使用的机器人数量（包括已完成工位，不包括当前工位）
 * 
 * 推导信息：
 * - remainingTasks = allTasks \ (completedStations的并集 ∪ currentStationTasks)
 * - remainingRobots = totalRobots - usedRobots - (currentStationHasRobot ? 1 : 0)
 */
public record NestedSALBPState(
    List<Set<Integer>> completedStations,      // 已完成的工位任务（按顺序）
    Set<Integer> currentStationTasks,           // 当前工位的任务
    boolean currentStationHasRobot,             // 当前工位是否有机器人
    int usedRobots) {                           // 已使用的机器人数

    public NestedSALBPState {
        // 防御性复制
        completedStations = List.copyOf(completedStations);
        currentStationTasks = Set.copyOf(currentStationTasks);
    }

    /**
     * 检查是否所有任务都已分配
     */
    public boolean isComplete(int totalTasks) {
        int assignedTasks = currentStationTasks.size();
        for (Set<Integer> station : completedStations) {
            assignedTasks += station.size();
        }
        return assignedTasks == totalTasks;
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
        for (Set<Integer> station : completedStations) {
            remaining.removeAll(station);
        }
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

    /**
     * 当前工位号（1-indexed）
     */
    public int currentStationNumber() {
        return completedStations.size() + 1;
    }

    /**
     * 已使用的工位数
     */
    public int getUsedStations() {
        return currentStationTasks.isEmpty() ? 
               completedStations.size() : 
               completedStations.size() + 1;
    }

    @Override
    public String toString() {
        return String.format("<Completed=%d, CS=%s, CSRobot=%s, usedRobots=%d>", 
            completedStations.size(), currentStationTasks, 
            currentStationHasRobot ? "Y" : "N",
            usedRobots);
    }
}
