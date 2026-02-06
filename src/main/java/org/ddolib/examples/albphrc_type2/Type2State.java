package org.ddolib.examples.albphrc_type2;

import java.util.*;

/**
 * 二型 ALBP-HRC 的状态表示
 * 
 * 目标：给定工位数 m，最小化节拍时间 C
 * 
 * 状态包含：
 * - completedTasks: 已完成任务集合
 * - stationIndex: 当前工位编号 (1..m)
 * - currentStationLoad: 当前工位的负载（makespan）
 * - currentStationHasRobot: 当前工位是否有机器人
 * - usedRobots: 已使用的机器人数量（不包括当前工位）
 * - maxLoadSoFar: 路径上的最大工位负载（目标函数）
 */
public class Type2State {
    
    private final Set<Integer> completedTasks;
    private final int stationIndex;              // 当前工位编号 (1-based)
    private final int currentStationLoad;        // 当前工位负载
    private final boolean currentStationHasRobot;
    private final int usedRobots;                // 已完成工位使用的机器人数
    private final int maxLoadSoFar;              // 当前路径的最大负载（目标函数）
    
    public Type2State(Set<Integer> completedTasks, 
                      int stationIndex,
                      int currentStationLoad,
                      boolean currentStationHasRobot,
                      int usedRobots,
                      int maxLoadSoFar) {
        this.completedTasks = Collections.unmodifiableSet(new LinkedHashSet<>(completedTasks));
        this.stationIndex = stationIndex;
        this.currentStationLoad = currentStationLoad;
        this.currentStationHasRobot = currentStationHasRobot;
        this.usedRobots = usedRobots;
        this.maxLoadSoFar = maxLoadSoFar;
    }
    
    public Set<Integer> completedTasks() {
        return completedTasks;
    }
    
    public int stationIndex() {
        return stationIndex;
    }
    
    public int currentStationLoad() {
        return currentStationLoad;
    }
    
    public boolean currentStationHasRobot() {
        return currentStationHasRobot;
    }
    
    public int usedRobots() {
        return usedRobots;
    }
    
    public int maxLoadSoFar() {
        return maxLoadSoFar;
    }
    
    /**
     * 检查是否所有任务都已完成
     */
    public boolean isComplete(int nbTasks) {
        return completedTasks.size() == nbTasks;
    }
    
    /**
     * 获取剩余未分配的任务
     */
    public Set<Integer> getRemainingTasks(int nbTasks) {
        Set<Integer> remaining = new LinkedHashSet<>();
        for (int i = 0; i < nbTasks; i++) {
            if (!completedTasks.contains(i)) {
                remaining.add(i);
            }
        }
        return remaining;
    }
    
    /**
     * 获取当前工位的任务集合
     * 注意：二型问题中，我们只需要知道当前工位的负载，不需要记录具体任务
     * 但为了支配规则和调试，我们可以通过 completedTasks 推断
     */
    public int remainingRobots(int totalRobots) {
        int used = usedRobots;
        if (currentStationHasRobot && stationIndex > 0) {
            used++;
        }
        return totalRobots - used;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Type2State that = (Type2State) o;
        return stationIndex == that.stationIndex &&
               currentStationLoad == that.currentStationLoad &&
               currentStationHasRobot == that.currentStationHasRobot &&
               usedRobots == that.usedRobots &&
               maxLoadSoFar == that.maxLoadSoFar &&
               Objects.equals(completedTasks, that.completedTasks);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(completedTasks, stationIndex, currentStationLoad, 
                           currentStationHasRobot, usedRobots, maxLoadSoFar);
    }
    
    @Override
    public String toString() {
        return String.format("State[completed=%d, station=%d, load=%d, robot=%s, usedRobots=%d, maxLoad=%d]",
                           completedTasks.size(), stationIndex, currentStationLoad,
                           currentStationHasRobot, usedRobots, maxLoadSoFar);
    }
}
