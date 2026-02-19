package org.ddolib.examples.ssalbrb1207nested;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.Relaxation;

import java.util.*;

/**
 * 外层问题的松弛：合并状态
 * 
 * 合并策略（创建真正的Relax状态）：
 * - completedTasks: 取交集（只有所有状态都认为已完成的任务）
 * - currentStationTasks: 取交集（只有所有状态都认为在当前工位的任务）
 * - maybeCompletedTasks: 取并集-交集（在某些状态中已完成或在当前工位，但不是所有状态）
 * - currentStationHasRobot: 取OR（任一为true则为true，容量更大）
 * - usedRobots: 取最小值（剩余机器人更多，更灵活）
 */
public class NestedSALBPRelax implements Relaxation<NestedSALBPState> {

    @Override
    public NestedSALBPState mergeStates(Iterator<NestedSALBPState> states) {
        if (!states.hasNext()) {
            throw new IllegalArgumentException("Cannot merge empty state set");
        }

        List<NestedSALBPState> stateList = new ArrayList<>();
        states.forEachRemaining(stateList::add);
        
        if (stateList.size() == 1) {
            return stateList.get(0);  // 只有一个状态，直接返回
        }

        // ========== 1. completedTasks: 取交集（保守） ==========
        Set<Integer> mergedCompleted = new LinkedHashSet<>(stateList.get(0).completedTasks());
        for (int i = 1; i < stateList.size(); i++) {
            mergedCompleted.retainAll(stateList.get(i).completedTasks());
        }

        // ========== 2. currentStationTasks: 取交集（乐观） ==========
        Set<Integer> mergedCurrent = new LinkedHashSet<>(stateList.get(0).currentStationTasks());
        for (int i = 1; i < stateList.size(); i++) {
            mergedCurrent.retainAll(stateList.get(i).currentStationTasks());
        }

        // ========== 3. maybeCompletedTasks: 并集 - 交集 ==========
        // 收集所有已分配的任务（包括原有的 maybeCompletedTasks）
        Set<Integer> allAssigned = new LinkedHashSet<>();
        for (NestedSALBPState s : stateList) {
            allAssigned.addAll(s.completedTasks());
            allAssigned.addAll(s.currentStationTasks());
            allAssigned.addAll(s.maybeCompletedTasks());
        }

        // maybeCompletedTasks = 所有已分配的任务 - 确定完成的 - 确定在当前工位的
        Set<Integer> mergedMaybe = new LinkedHashSet<>(allAssigned);
        mergedMaybe.removeAll(mergedCompleted);
        mergedMaybe.removeAll(mergedCurrent);

        // ========== 4. currentStationHasRobot: 取OR（任一为true则为true） ==========
        boolean mergedHasRobot = stateList.stream()
            .anyMatch(NestedSALBPState::currentStationHasRobot);

        // ========== 5. usedRobots: 取最小值（最乐观） ==========
        int mergedUsedRobots = stateList.stream()
            .mapToInt(NestedSALBPState::usedRobots)
            .min()
            .orElse(0);

        return new NestedSALBPState(
            mergedCompleted,
            mergedCurrent,
            mergedMaybe,
            mergedHasRobot,
            mergedUsedRobots
        );
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
