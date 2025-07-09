package org.ddolib.ddo.core.profiling;

public record SearchStatistics(int nbIterations, int queueMaxSize, long runTimeMS) {

    @Override
    public String toString() {
        return "SearchStatistics{" +
                "nbIterations=" + nbIterations +
                ", queueMaxSize=" + queueMaxSize +
                ", runTimeMS=" + runTimeMS +
                '}';
    }
}
