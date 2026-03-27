package org.ddolib.examples.ssalbrb1207nested;

import org.ddolib.modeling.StateRanking;

/**
 * 外层状态排序：优先选择更有潜力的状态进行扩展
 *
 * 排序策略（按优先级从高到低）：
 * 1. 总工位数：越少越好（主要目标）
 * 2. 当前工位任务数：越多越好（充分利用当前工位，减少总工位数）
 * 3. Maybe完成任务数：越多越好（表示更多任务可能已完成，下界更紧）
 * 4. 当前工位机器人：有机器人优先（提高当前工位容量）
 * 5. 已使用机器人数：越少越好（保留更多机器人给未来）
 */
public class NestedSALBPRanking implements StateRanking<NestedSALBPState> {

    private final int totalRobots;

    public NestedSALBPRanking(int totalRobots) {
        this.totalRobots = totalRobots;
    }

    @Override
    public int compare(NestedSALBPState first, NestedSALBPState second) {
        // 1. 首先比较总工位数（已完成 + 当前工位）- 越少越好
        int stationsCompare = Integer.compare(
                first.totalStations(),
                second.totalStations()
        );
        if (stationsCompare != 0) {
            return stationsCompare;
        }

        // 2. 其次比较当前工位任务数（越多越好 - 充分利用当前工位）
        int currentTasksCompare = Integer.compare(
                second.currentStationTasks().size(),  // second 在前，表示越大越好
                first.currentStationTasks().size()
        );
        if (currentTasksCompare != 0) {
            return currentTasksCompare;
        }

        // 3. 比较 maybeCompletedTasks 数量（越多越好 - 表示更多任务可能已完成）
        int maybeCompare = Integer.compare(
                second.maybeCompletedTasks().size(),  // second 在前，表示越大越好
                first.maybeCompletedTasks().size()
        );
        if (maybeCompare != 0) {
            return maybeCompare;
        }

        // 4. 如果当前工位有机器人，优先（提高当前工位容量）
        int currentRobotCompare = Boolean.compare(
                second.currentStationHasRobot(),  // true > false
                first.currentStationHasRobot()
        );
        if (currentRobotCompare != 0) {
            return currentRobotCompare;
        }

        // 5. 比较已使用的机器人数（越少越好 - 保留更多机器人给未来）
        return Integer.compare(
                first.usedRobots(),
                second.usedRobots()
        );
    }
}
