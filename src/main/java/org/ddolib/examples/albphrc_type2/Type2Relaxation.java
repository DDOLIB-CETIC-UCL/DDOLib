package org.ddolib.examples.albphrc_type2;

import org.ddolib.modeling.Relaxation;
import org.ddolib.ddo.core.Decision;

import java.util.Iterator;

/**
 * 二型 ALBP-HRC 的松弛操作
 * 
 * 当需要合并多个状态时，选择"最有潜力"的代表状态
 * 
 * 选择标准（优先级从高到低）：
 * 1. maxLoadSoFar 更小（目标函数更优）
 * 2. stationIndex 更小（使用更少工位，更灵活）
 * 3. currentStationLoad 更小（当前工位有更多空间）
 * 4. usedRobots 更少（保留更多机器人资源）
 * 5. currentStationHasRobot = true（当前工位有机器人更灵活）
 */
public class Type2Relaxation implements Relaxation<Type2State> {
    
    @Override
    public Type2State mergeStates(Iterator<Type2State> states) {
        if (!states.hasNext()) {
            return null;
        }
        
        Type2State best = states.next();
        while (states.hasNext()) {
            Type2State current = states.next();
            best = merge(best, current);
        }
        return best;
    }
    
    @Override
    public double relaxEdge(Type2State from, Type2State to, Type2State merged, Decision d, double cost) {
        // 对于二型问题，边的代价是 maxLoad 的增量
        // 松弛后的代价应该是从 from 到 merged 的代价
        // 这里我们使用简单的策略：保持原代价
        return cost;
    }
    
    private Type2State merge(Type2State state1, Type2State state2) {
        // 优先级1：maxLoadSoFar 更小
        if (state1.maxLoadSoFar() != state2.maxLoadSoFar()) {
            return state1.maxLoadSoFar() < state2.maxLoadSoFar() ? state1 : state2;
        }
        
        // 优先级2：currentStationLoad 更小（避免单个工位过载）
        if (state1.currentStationLoad() != state2.currentStationLoad()) {
            return state1.currentStationLoad() < state2.currentStationLoad() ? state1 : state2;
        }
        
        // 优先级3：stationIndex 更大（更多地利用工位）
        if (state1.stationIndex() != state2.stationIndex()) {
            return state1.stationIndex() > state2.stationIndex() ? state1 : state2;
        }
        
        // 优先级4：usedRobots 更少
        if (state1.usedRobots() != state2.usedRobots()) {
            return state1.usedRobots() < state2.usedRobots() ? state1 : state2;
        }
        
        // 优先级5：有机器人的工位更优
        if (state1.currentStationHasRobot() != state2.currentStationHasRobot()) {
            return state1.currentStationHasRobot() ? state1 : state2;
        }
        
        // 如果所有条件都相同，返回第一个
        return state1;
    }
}
