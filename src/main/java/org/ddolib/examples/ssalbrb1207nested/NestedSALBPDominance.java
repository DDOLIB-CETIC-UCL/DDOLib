package org.ddolib.examples.ssalbrb1207nested;

import org.ddolib.modeling.Dominance;
import java.util.Objects;

/**
 * Dominance relation for the Nested SALBP Problem.
 * <p>
 * 状态支配规则用于剪枝，当一个状态被另一个状态支配时，可以安全地丢弃被支配的状态。
 * </p>
 *
 * <p>
 * 对于两个状态 s1 和 s2，如果满足以下条件，则 s1 被 s2 支配（s2 dominate s1）：
 * </p>
 * <ul>
 *   <li><b>前提条件</b>：completedTasks 相同且 currentStationTasks 相同</li>
 *   <li><b>规则1 - 机器人优势</b>：当 usedRobots 相等时，有机器人的工位优于没机器人的工位
 *       （因为有机器人的工位可以接受更多任务，搜索空间更大）</li>
 *   <li><b>规则2 - 机器人资源</b>：剩余机器人更多的状态优于剩余机器人更少的状态
 *       （因为未来有更多选择）</li>
 *   <li><b>综合规则</b>：s2 dominate s1 当且仅当 s2 的"总消耗机器人"不多于 s1 的
 *       且 s2 当前工位的机器人状态不差于 s1</li>
 * </ul>
 */
public class NestedSALBPDominance implements Dominance<NestedSALBPState> {

    /**
     * 支配关系的分组键
     * 只有 completedTasks 和 currentStationTasks 都相同的状态才需要比较支配关系
     */
    private record DominanceKey(
        java.util.Set<Integer> completedTasks,
        java.util.Set<Integer> currentStationTasks
    ) {}

    /**
     * 返回用于分组的键。
     * 只有相同 key 的状态才会进行支配关系比较。
     * 
     * @param state 当前状态
     * @return 分组键（completedTasks + currentStationTasks）
     */
    @Override
    public Object getKey(NestedSALBPState state) {
        return new DominanceKey(state.completedTasks(), state.currentStationTasks());
    }

    /**
     * 判断 state1 是否被 state2 支配（或相等）。
     * <p>
     * 支配判断逻辑：
     * </p>
     * <ol>
     *   <li>计算总使用机器人数 = usedRobots + (currentStationHasRobot ? 1 : 0)</li>
     *   <li>如果 state2 总使用机器人 < state1 总使用机器人，则 state2 更优，state1 被支配</li>
     *   <li>如果总使用机器人相等：
     *       <ul>
     *         <li>若 state2 当前有机器人而 state1 没有，则 state1 被支配（规则1）</li>
     *         <li>若机器人状态也相同，则两者等价</li>
     *       </ul>
     *   </li>
     * </ol>
     * 
     * @param state1 第一个状态
     * @param state2 第二个状态
     * @return true 如果 state1 被 state2 支配或两者等价
     */
    @Override
    public boolean isDominatedOrEqual(NestedSALBPState state1, NestedSALBPState state2) {
        // 计算总使用机器人数（包括当前工位）
        int totalUsed1 = state1.usedRobots() + (state1.currentStationHasRobot() ? 1 : 0);
        int totalUsed2 = state2.usedRobots() + (state2.currentStationHasRobot() ? 1 : 0);

        // 规则2：state2 使用更少的机器人 -> state2 更优 -> state1 被支配
        if (getKey(state1).equals(getKey(state2)) && totalUsed2 < totalUsed1) {
            return true;
        }

        // 规则1：总使用机器人相等时，比较当前工位的机器人状态
        if (getKey(state1).equals(getKey(state2)) && totalUsed1 == totalUsed2) {
            // usedRobots 相等的情况
            if (state1.usedRobots() == state2.usedRobots()) {
                // 若 state2 有机器人而 state1 没有 -> state1 被支配
                // 若两者机器人状态相同 -> 等价
                // 若 state1 有机器人而 state2 没有 -> state1 不被支配
                if (!state1.currentStationHasRobot() && state2.currentStationHasRobot()) {
                    return true;  // state1 被 state2 支配
                }
                if (state1.currentStationHasRobot() == state2.currentStationHasRobot()) {
                    return true;  // 等价
                }
            }
            // usedRobots 不等但 totalUsed 相等的情况
            // 例如：state1(usedRobots=2, hasRobot=false) vs state2(usedRobots=1, hasRobot=true)
            // 两者 totalUsed=2，但 state2 当前有机器人，未来更灵活
            // 这种情况下 state2 略优，但不是严格支配，保守起见不剪枝
        }

        return false;
    }
}
