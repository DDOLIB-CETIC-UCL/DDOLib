package org.ddolib.common.solver;

public record SearchStatistics(
        SearchStatus status,
        int nbIterations,
        int queueMaxSize,
        long runTimeMs,
        double incumbent,
        double gap) {

    @Override
    public String toString() {
        return "SearchStatistics{" +
                "status=" + status +
                ", nbIterations=" + nbIterations +
                ", queueMaxSize=" + queueMaxSize +
                ", runTimeMs=" + runTimeMs +
                ", incumbent=" + (incumbent == Double.POSITIVE_INFINITY || incumbent == Double.NEGATIVE_INFINITY ? "+-∞" : incumbent) +
                ", gap=" + (gap == Double.POSITIVE_INFINITY ? "∞" : gap) +
                '}';
    }
}

