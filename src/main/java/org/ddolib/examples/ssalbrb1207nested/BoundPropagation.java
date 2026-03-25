package org.ddolib.examples.ssalbrb1207nested;

/**
 * Bound Propagation
 * 
 * Principle: Use known optimal solution upper bound for pruning
 * If current state's lower bound ≥ known upper bound, can prune
 * 
 * Corresponds to Branch and Bound in MIP:
 * - MIP: If node's LP relaxation ≥ current best solution, prune
 * - DDO: If state's lower bound + current cost ≥ current best solution, prune
 */
public class BoundPropagation {
    
    private int currentBestSolution = Integer.MAX_VALUE;
    private long updateCount = 0;
    private long checkCount = 0;
    private long pruneCount = 0;
    
    /**
     * Update best solution upper bound
     * 
     * @param solutionValue Objective value of newly found solution
     */
    public void updateBestSolution(int solutionValue) {
        if (solutionValue < currentBestSolution) {
            int improvement = currentBestSolution - solutionValue;
            currentBestSolution = solutionValue;
            updateCount++;
            
            System.out.printf("[BoundProp] Update upper bound: %d (improvement: %d)%n", 
                    solutionValue, improvement == Integer.MAX_VALUE ? 0 : improvement);
        }
    }
    
    /**
     * Check if pruning is possible
     * 
     * @param currentValue Number of stations currently used
     * @param lowerBound Lower bound on number of stations needed for remaining tasks
     * @return true if can prune
     */
    public boolean canPrune(int currentValue, double lowerBound) {
        checkCount++;
        
        // If no solution found yet, cannot prune
        if (currentBestSolution == Integer.MAX_VALUE) {
            return false;
        }
        
        // Total lower bound = current value + future lower bound
        double totalLowerBound = currentValue + lowerBound;
        
        // If lower bound ≥ upper bound, prune
        if (totalLowerBound >= currentBestSolution) {
            pruneCount++;
            return true;
        }
        
        return false;
    }
    
    /**
     * Get current best solution
     */
    public int getCurrentBestSolution() {
        return currentBestSolution;
    }
    
    /**
     * Get prune count
     */
    public long getPruneCount() {
        return pruneCount;
    }
    
    /**
     * Get check count
     */
    public long getCheckCount() {
        return checkCount;
    }
    
    /**
     * Get update count
     */
    public long getUpdateCount() {
        return updateCount;
    }
    
    /**
     * Get statistics information
     */
    public String getStatistics() {
        return String.format("BoundPropagation: checks=%d, prunes=%d, updates=%d, bestUB=%d, pruneRate=%.2f%%",
                checkCount, pruneCount, updateCount, 
                currentBestSolution == Integer.MAX_VALUE ? -1 : currentBestSolution,
                checkCount > 0 ? (100.0 * pruneCount / checkCount) : 0.0);
    }
    
    /**
     * Reset statistics
     */
    public void reset() {
        currentBestSolution = Integer.MAX_VALUE;
        updateCount = 0;
        checkCount = 0;
        pruneCount = 0;
    }
}
