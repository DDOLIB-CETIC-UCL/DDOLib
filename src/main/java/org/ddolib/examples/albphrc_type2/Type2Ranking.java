package org.ddolib.examples.albphrc_type2;

import org.ddolib.modeling.StateRanking;

/**
 * 二型 ALBP-HRC 的状态排序启发式
 * 
 * 用于 beam search 中决定优先扩展哪些状态
 * 
 * 排序标准（优先级从高到低）：
 * 1. maxLoadSoFar 更小（目标函数更优）
 * 2. 当前工位负载更小（避免负载不平衡）
 * 3. completedTasks 更多（更接近终点）
 * 4. stationIndex 更大（更多地利用工位）
 * 5. usedRobots 更少（保留更多资源）
 */
public class Type2Ranking implements StateRanking<Type2State> {
    
    @Override
    public int compare(Type2State s1, Type2State s2) {
        // 优先级1：maxLoadSoFar 更小（目标函数）
        int cmp = Integer.compare(s1.maxLoadSoFar(), s2.maxLoadSoFar());
        if (cmp != 0) return cmp;
        
        // 优先级2：当前工位负载更小（避免单个工位过载）
        cmp = Integer.compare(s1.currentStationLoad(), s2.currentStationLoad());
        if (cmp != 0) return cmp;
        
        // 优先级3：completedTasks 更多
        cmp = Integer.compare(s2.completedTasks().size(), s1.completedTasks().size());
        if (cmp != 0) return cmp;
        
        // 优先级4：stationIndex 更大（更多地利用工位）
        cmp = Integer.compare(s2.stationIndex(), s1.stationIndex());
        if (cmp != 0) return cmp;
        
        // 优先级5：usedRobots 更少
        cmp = Integer.compare(s1.usedRobots(), s2.usedRobots());
        if (cmp != 0) return cmp;
        
        return 0;
    }
}
