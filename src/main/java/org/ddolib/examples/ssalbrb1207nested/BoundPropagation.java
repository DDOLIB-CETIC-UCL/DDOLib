package org.ddolib.examples.ssalbrb1207nested;

/**
 * 上界传播（Bound Propagation）
 * 
 * 原理：利用已知的最优解上界进行剪枝
 * 如果当前状态的下界 ≥ 已知上界，则可以剪枝
 * 
 * 对应MIP中的分支定界：
 * - MIP: 如果节点的LP松弛值 ≥ 当前最优解，剪枝
 * - DDO: 如果状态的下界 + 当前代价 ≥ 当前最优解，剪枝
 */
public class BoundPropagation {
    
    private int currentBestSolution = Integer.MAX_VALUE;
    private long updateCount = 0;
    private long checkCount = 0;
    private long pruneCount = 0;
    
    /**
     * 更新最优解上界
     * 
     * @param solutionValue 新找到的解的目标值
     */
    public void updateBestSolution(int solutionValue) {
        if (solutionValue < currentBestSolution) {
            int improvement = currentBestSolution - solutionValue;
            currentBestSolution = solutionValue;
            updateCount++;
            
            System.out.printf("[BoundProp] 更新上界: %d (改进: %d)%n", 
                    solutionValue, improvement == Integer.MAX_VALUE ? 0 : improvement);
        }
    }
    
    /**
     * 检查是否可以剪枝
     * 
     * @param currentValue 当前已使用的工位数
     * @param lowerBound 剩余任务需要的工位数下界
     * @return true 如果可以剪枝
     */
    public boolean canPrune(int currentValue, double lowerBound) {
        checkCount++;
        
        // 如果还没有找到任何解，不能剪枝
        if (currentBestSolution == Integer.MAX_VALUE) {
            return false;
        }
        
        // 总下界 = 当前值 + 未来下界
        double totalLowerBound = currentValue + lowerBound;
        
        // 如果下界 ≥ 上界，剪枝
        if (totalLowerBound >= currentBestSolution) {
            pruneCount++;
            return true;
        }
        
        return false;
    }
    
    /**
     * 获取当前最优解
     */
    public int getCurrentBestSolution() {
        return currentBestSolution;
    }
    
    /**
     * 获取剪枝次数
     */
    public long getPruneCount() {
        return pruneCount;
    }
    
    /**
     * 获取检查次数
     */
    public long getCheckCount() {
        return checkCount;
    }
    
    /**
     * 获取更新次数
     */
    public long getUpdateCount() {
        return updateCount;
    }
    
    /**
     * 获取统计信息
     */
    public String getStatistics() {
        return String.format("BoundPropagation: checks=%d, prunes=%d, updates=%d, bestUB=%d, pruneRate=%.2f%%",
                checkCount, pruneCount, updateCount, 
                currentBestSolution == Integer.MAX_VALUE ? -1 : currentBestSolution,
                checkCount > 0 ? (100.0 * pruneCount / checkCount) : 0.0);
    }
    
    /**
     * 重置统计信息
     */
    public void reset() {
        currentBestSolution = Integer.MAX_VALUE;
        updateCount = 0;
        checkCount = 0;
        pruneCount = 0;
    }
}
