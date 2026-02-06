package org.ddolib.examples.albphrc_type2;

import org.ddolib.modeling.Dominance;

import java.util.*;

/**
 * 二型 ALBP-HRC 的支配规则
 * 
 * 状态 s1 被 s2 支配（或相等）当且仅当：
 * 1. completedTasks 相同（处于相同的决策点）
 * 2. stationIndex 相同（使用相同数量的工位）
 * 3. s1.maxLoadSoFar >= s2.maxLoadSoFar（s2 的目标函数更优）
 * 4. s1.currentStationLoad >= s2.currentStationLoad（s2 的当前工位负载更优）
 * 5. s1.usedRobots >= s2.usedRobots（s2 使用更少机器人）
 * 6. 如果 s2.currentStationHasRobot = true 且 s1 = false，则 s1 被 s2 支配
 */
public class Type2Dominance implements Dominance<Type2State> {
    
    @Override
    public Object getKey(Type2State state) {
        // 使用 completedTasks 和 stationIndex 作为 key
        // 只有相同 key 的状态才会进行支配比较
        return Arrays.asList(state.completedTasks(), state.stationIndex());
    }
    
    @Override
    public boolean isDominatedOrEqual(Type2State s1, Type2State s2) {
        // 注意：isDominatedOrEqual(s1, s2) 返回 true 表示 s1 被 s2 支配或相等
        // 即 s2 至少和 s1 一样好
        
        // 条件1：completedTasks 必须相同
        if (!s1.completedTasks().equals(s2.completedTasks())) {
            return false;
        }
        
        // 条件2：stationIndex 必须相同
        if (s1.stationIndex() != s2.stationIndex()) {
            return false;
        }
        
        // 条件3：s2 的 maxLoadSoFar 不能更差（s2 <= s1）
        if (s2.maxLoadSoFar() > s1.maxLoadSoFar()) {
            return false;
        }
        
        // 条件4：s2 的 currentStationLoad 不能更差
        if (s2.currentStationLoad() > s1.currentStationLoad()) {
            return false;
        }
        
        // 条件5：s2 的 usedRobots 不能更多
        if (s2.usedRobots() > s1.usedRobots()) {
            return false;
        }
        
        // 条件6：机器人配置
        // 如果 s2 有机器人而 s1 没有，s2 更优（更灵活）
        // 如果 s2 没有机器人而 s1 有，需要检查其他条件是否严格更优
        if (!s2.currentStationHasRobot() && s1.currentStationHasRobot()) {
            // s2 没有机器人，s1 有机器人
            // 只有当 s2 在其他方面严格更优时才能支配 s1
            return s2.maxLoadSoFar() < s1.maxLoadSoFar() ||
                   s2.currentStationLoad() < s1.currentStationLoad() ||
                   s2.usedRobots() < s1.usedRobots();
        }
        
        // 如果所有条件都满足，则 s1 被 s2 支配或相等
        return true;
    }
}
