package org.ddolib.ddo.core;

public record SearchStatistics(int nbIterations, int queueMaxSize, long runTimeMS, SearchStatus SearchStatus) {

    public enum SearchStatus {
        OPTIMAL, UNSAT, SAT, UNKNOWN;
    }
    @Override
    public String toString() {
        return "SearchStatistics{" +
                "nbIterations=" + nbIterations +
                ", queueMaxSize=" + queueMaxSize +
                ", runTimeMS=" + runTimeMS +
                ", SearchStatus=" + SearchStatus +
                '}';
    }
}
