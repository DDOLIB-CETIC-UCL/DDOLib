package org.ddolib.examples.hrcps;

/**
 * Bound Propagation: uses the incumbent upper bound for pruning.
 * If current state's lower bound ≥ best known solution, prune.
 */
public class BoundPropagation {

    private int currentBestSolution = Integer.MAX_VALUE;
    private long updateCount = 0;
    private long checkCount = 0;
    private long pruneCount = 0;

    public void updateBestSolution(int solutionValue) {
        if (solutionValue < currentBestSolution) {
            currentBestSolution = solutionValue;
            updateCount++;
            System.out.printf("[BoundProp] Updated upper bound: %d%n", solutionValue);
        }
    }

    public boolean canPrune(int currentValue, double lowerBound) {
        checkCount++;
        if (currentBestSolution == Integer.MAX_VALUE) return false;
        if (currentValue + lowerBound >= currentBestSolution) {
            pruneCount++;
            return true;
        }
        return false;
    }

    public int getCurrentBestSolution() { return currentBestSolution; }
    public long getPruneCount() { return pruneCount; }
    public long getCheckCount() { return checkCount; }
    public long getUpdateCount() { return updateCount; }

    public String getStatistics() {
        return String.format("BoundPropagation: checks=%d, prunes=%d, updates=%d, bestUB=%d, rate=%.2f%%",
                checkCount, pruneCount, updateCount,
                currentBestSolution == Integer.MAX_VALUE ? -1 : currentBestSolution,
                checkCount > 0 ? (100.0 * pruneCount / checkCount) : 0.0);
    }

    public void reset() {
        currentBestSolution = Integer.MAX_VALUE;
        updateCount = checkCount = pruneCount = 0;
    }
}

