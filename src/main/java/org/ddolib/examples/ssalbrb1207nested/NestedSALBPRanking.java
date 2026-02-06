package org.ddolib.examples.ssalbrb1207nested;

import org.ddolib.modeling.StateRanking;

/**
 * 外层状态排序：优先选择工位数少的状态
 */
public class NestedSALBPRanking implements StateRanking<NestedSALBPState> {

    private final int totalRobots;

    public NestedSALBPRanking(int totalRobots) {
        this.totalRobots = totalRobots;
    }

    @Override
    public int compare(NestedSALBPState first, NestedSALBPState second) {
        // 首先比较已完成任务数（越少越好，近似工位数）
        int completedCompare = Integer.compare(
                first.completedTasks().size(),
                second.completedTasks().size()
        );
        if (completedCompare != 0) {
            return completedCompare;
        }

        // 其次比较当前工位任务数（越多越好 - 在当前工位塞入更多任务意味着更少的总工位数）
        int currentTasksCompare = Integer.compare(
                second.currentStationTasks().size(),  // 注意：second 在前，表示越大越好
                first.currentStationTasks().size()
        );
        if (currentTasksCompare != 0) {
            return currentTasksCompare;
        }

        // 第三，如果当前工位有机器人，优先（提高当前工位容量，能塞入更多任务）
        int currentRobotCompare = Boolean.compare(
                second.currentStationHasRobot(),  // 注意顺序：true > false
                first.currentStationHasRobot()
        );
        if (currentRobotCompare != 0) {
            return currentRobotCompare;
        }

        // 第四，比较已使用的机器人数（越少越好 - 保留更多机器人给未来）
        int usedRobotsCompare = Integer.compare(
                first.usedRobots(),  // 注意顺序：first 在前表示越小越好
                second.usedRobots()
        );
        if (usedRobotsCompare != 0) {
            return usedRobotsCompare;
        }

        // 最后比较剩余机器人数（越多越好）
        return Integer.compare(second.remainingRobots(totalRobots), first.remainingRobots(totalRobots));
    }
}
