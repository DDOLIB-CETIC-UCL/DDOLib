package org.ddolib.examples.ssalbrb1207nested;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.Relaxation;

import java.util.*;

/**
 * 外层问题的松弛：合并状态
 * 取最少的已完成工位数（更乐观），合并当前工位任务（更宽松）
 */
public class NestedSALBPRelax implements Relaxation<NestedSALBPState> {

    @Override
    public NestedSALBPState mergeStates(Iterator<NestedSALBPState> states) {
        if (!states.hasNext()) {
            throw new IllegalArgumentException("Cannot merge empty state set");
        }

        // 对于有容量约束的问题，简单的合并策略很难满足DDO放松要求
        // 最安全的策略：选择"最佳"状态作为放松状态
        //
        // "最佳"定义（按优先级）：
        // 1. 当前工位有机器人（CSRobot=Y）- 工位容量更大，能容纳更多任务
        // 2. 当前工位任务数最少 - 给后续任务留更多空间
        // 3. 已完成工位数最少
        // 4. 已使用机器人数最少

        NestedSALBPState bestState = states.next();

        while (states.hasNext()) {
            NestedSALBPState state = states.next();

            // 比较函数：返回 true 如果 state 比 bestState 更好
            boolean stateIsBetter = false;

            // 1. 优先选择当前工位有机器人的（容量更大）
            if (state.currentStationHasRobot() && !bestState.currentStationHasRobot()) {
                stateIsBetter = true;
            } else if (state.currentStationHasRobot() == bestState.currentStationHasRobot()) {
                // 2. 当前工位任务数更少
                int bestCsSize = bestState.currentStationTasks().size();
                int stateCsSize = state.currentStationTasks().size();

                if (stateCsSize < bestCsSize) {
                    stateIsBetter = true;
                } else if (stateCsSize == bestCsSize) {
                    // 3. 已完成工位数更少
                    int bestCompleted = bestState.completedStations().size();
                    int stateCompleted = state.completedStations().size();

                    if (stateCompleted < bestCompleted) {
                        stateIsBetter = true;
                    } else if (stateCompleted == bestCompleted) {
                        // 4. 已使用机器人数更少
                        if (state.usedRobots() < bestState.usedRobots()) {
                            stateIsBetter = true;
                        }
                    }
                }
            }

            if (stateIsBetter) {
                bestState = state;
            }
        }

        return bestState;
    }

    @Override
    public double relaxEdge(NestedSALBPState from,
                            NestedSALBPState to,
                            NestedSALBPState merged,
                            Decision decision,
                            double originalCost) {
        return originalCost;
    }
}
