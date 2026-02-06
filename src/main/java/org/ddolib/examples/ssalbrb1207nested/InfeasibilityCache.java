package org.ddolib.examples.ssalbrb1207nested;

import java.util.*;

/**
 * 不可行任务子集缓存
 * 用于剪枝：如果任务集合S不可行，则任何包含S的超集也不可行
 *
 * 优化策略：
 * 1. 记录不可行的最小任务子集（minimal infeasible sets）
 * 2. 在判断可行性前，先检查是否包含已知的不可行子集
 * 3. 减少内层DDO调用次数
 */
public class InfeasibilityCache {

    // 存储不可行的任务子集（按大小分组，便于快速查找）
    private final Map<Integer, Set<Set<Integer>>> infeasibleSets;

    // 统计信息
    private long pruneCount = 0;
    private long checkCount = 0;

    public InfeasibilityCache() {
        this.infeasibleSets = new HashMap<>();
    }

    /**
     * 记录一个不可行的任务集合
     * 只记录最小不可行子集（minimal infeasible set）
     *
     * @param tasks 不可行的任务集合
     * @param hasRobot 是否有机器人
     */
    public void recordInfeasible(Set<Integer> tasks, boolean hasRobot) {
        if (tasks.isEmpty()) return;

        // 只记录有机器人情况下的不可行集合
        // 因为无机器人的不可行不能推广到有机器人的情况
        if (!hasRobot) return;

        // 检查是否已经有更小的不可行子集
        if (containsInfeasibleSubset(tasks)) {
            return; // 已经有更小的不可行子集，不需要记录
        }

        // 移除所有包含当前集合的更大不可行集合（它们是冗余的）
        removeSupersetsOf(tasks);

        // 记录当前不可行集合
        int size = tasks.size();
        infeasibleSets.computeIfAbsent(size, k -> new HashSet<>()).add(Set.copyOf(tasks));
    }

    /**
     * 检查任务集合是否包含已知的不可行子集
     *
     * @param tasks 要检查的任务集合
     * @return true 如果包含不可行子集（可以剪枝）
     */
    public boolean containsInfeasibleSubset(Set<Integer> tasks) {
        checkCount++;

        if (tasks.isEmpty()) return false;

        // 只需要检查大小 <= tasks.size() 的不可行集合
        for (int size = 1; size <= tasks.size(); size++) {
            Set<Set<Integer>> setsOfSize = infeasibleSets.get(size);
            if (setsOfSize == null) continue;

            for (Set<Integer> infeasibleSet : setsOfSize) {
                // 检查 infeasibleSet 是否是 tasks 的子集
                if (tasks.containsAll(infeasibleSet)) {
                    pruneCount++;
                    return true; // 找到不可行子集，可以剪枝
                }
            }
        }

        return false;
    }

    /**
     * 移除所有包含给定集合的超集（它们是冗余的）
     */
    private void removeSupersetsOf(Set<Integer> tasks) {
        for (int size = tasks.size() + 1; size <= 20; size++) { // 假设最大工位任务数不超过20
            Set<Set<Integer>> setsOfSize = infeasibleSets.get(size);
            if (setsOfSize == null) continue;

            setsOfSize.removeIf(set -> set.containsAll(tasks));
        }
    }

    /**
     * 获取剪枝次数（用于外部统计）
     */
    public long getPruneCount() {
        return pruneCount;
    }

    /**
     * 获取检查次数（用于外部统计）
     */
    public long getCheckCount() {
        return checkCount;
    }

    /**
     * 获取存储的不可行集合数量
     */
    public int getStoredCount() {
        return infeasibleSets.values().stream()
                .mapToInt(Set::size)
                .sum();
    }

    /**
     * 获取统计信息
     */
    public String getStatistics() {
        int totalInfeasibleSets = getStoredCount();

        return String.format("InfeasibilityCache: checks=%d, prunes=%d, stored=%d, pruneRate=%.2f%%",
                checkCount, pruneCount, totalInfeasibleSets,
                checkCount > 0 ? (100.0 * pruneCount / checkCount) : 0.0);
    }

    /**
     * 清空缓存
     */
    public void clear() {
        infeasibleSets.clear();
        pruneCount = 0;
        checkCount = 0;
    }
}
