package org.ddolib.examples.ssalbrb1207nested;

import org.ddolib.modeling.Dominance;
import java.util.HashSet;
import java.util.Set;

/**
 * 支配规则（Dominance Rule）
 *
 * 状态支配规则用于剪枝，当一个状态被另一个状态支配时，可以安全地丢弃被支配的状态。
 *
 * 支配规则设计：
 *
 * 前提条件：
 *   - 已分配任务集合（completedTasks ∪ currentStationTasks ∪ maybeCompletedTasks）相同
 *   - 即：两个状态面临相同的剩余任务分配问题
 *
 * 支配判断规则（多维度资源支配）：
 *   维度1 - 工位数：已使用的工位数越少越好
 *           （工位数 = stationNumber + (当前工位非空 ? 1 : 0)）
 *
 *   维度2 - 机器人总数：已使用的机器人总数越少越好
 *           （机器人总数 = usedRobots + (当前工位有机器人 ? 1 : 0)）
 *
 *   维度3 - 机器人分配灵活性：在总资源相同时，机器人在当前工位比在已关闭工位更优
 *           （因为当前工位的机器人配置尚未固定，保留了更多调整空间）
 *
 * 支配情况（state2 支配 state1）：
 *   情况1：state2 工位更少，且机器人不多于 state1
 *   情况2：state2 机器人更少，且工位不多于 state1
 *   情况3：工位数和机器人总数都相同，但 state2 的机器人分配更灵活
 *          （更多机器人在当前工位，更少在已关闭工位）
 *
 * 理论正确性：
 *   如果两个状态的"已分配任务集合"相同，则它们面临相同的"剩余任务分配问题"。
 *   此时，使用更少资源或资源分配更灵活的状态拥有更多未来选择，因此不会更差。
 */
public class NestedSALBPDominance implements Dominance<NestedSALBPState> {

    /**
     * 支配关系的分组键
     *
     * 策略：只要"所有已分配任务"相同就比较，不管这些任务如何分配到工位
     */
    private record DominanceKey(
            Set<Integer> allAssignedTasks  // completedTasks ∪ currentStationTasks ∪ maybeCompletedTasks
    ) {}

    /**
     * 返回用于分组的键。
     * 只有相同 key 的状态才会进行支配关系比较。
     *
     * 只要"已分配任务集合"相同，就进行支配关系比较，
     * 而不管这些任务是如何分配到工位的。
     *
     * @param state 当前状态
     * @return 分组键（所有已分配任务）
     */
    @Override
    public Object getKey(NestedSALBPState state) {
        // 合并 completedTasks, currentStationTasks 和 maybeCompletedTasks
        Set<Integer> allAssigned = new HashSet<>(state.completedTasks());
        allAssigned.addAll(state.currentStationTasks());
        allAssigned.addAll(state.maybeCompletedTasks());
        return new DominanceKey(allAssigned);
    }

    /**
     * 判断 state1 是否被 state2 支配或等价
     *
     * @param state1 第一个状态
     * @param state2 第二个状态
     * @return true 如果 state1 被 state2 支配或两者等价
     */
    @Override
    public boolean isDominatedOrEqual(NestedSALBPState state1, NestedSALBPState state2) {
        // 计算维度1：已使用的工位数（精确值）
        int stations1 = state1.stationNumber() + (state1.currentStationTasks().isEmpty() ? 0 : 1);
        int stations2 = state2.stationNumber() + (state2.currentStationTasks().isEmpty() ? 0 : 1);

        // 计算维度2：已使用的机器人总数（包括当前工位）
        int totalUsed1 = state1.usedRobots() + (state1.currentStationHasRobot() ? 1 : 0);
        int totalUsed2 = state2.usedRobots() + (state2.currentStationHasRobot() ? 1 : 0);

        // 情况1：state2 工位更少，且机器人不多于 state1 → state2 支配 state1
        if (stations2 < stations1 && totalUsed2 <= totalUsed1) {
            return true;
        }

        // 情况2：state2 机器人更少，且工位不多于 state1 → state2 支配 state1
        if (stations2 <= stations1 && totalUsed2 < totalUsed1) {
            return true;
        }

        // 情况3：工位数和机器人总数都相同，比较机器人分配的灵活性
        if (stations2 == stations1 && totalUsed2 == totalUsed1) {
            // state2 的已关闭工位使用更少机器人 → state2 的当前工位有机器人 → 更灵活
            // 因为当前工位的机器人配置尚未固定，可以根据后续任务调整
            if (state2.usedRobots() < state1.usedRobots()) {
                return true;  // state2 支配 state1
            }
            // 机器人分配方式也相同 → 两者等价
            if (state2.usedRobots() == state1.usedRobots()) {
                return true;  // 等价
            }
        }

        return false;
    }
}
