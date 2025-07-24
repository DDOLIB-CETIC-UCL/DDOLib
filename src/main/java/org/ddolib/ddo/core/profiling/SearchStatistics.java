package org.ddolib.ddo.core.profiling;

public record SearchStatistics(int nbIterations, int queueMaxSize, long runTimeMS, SearchStatus SearchStatus, double Gap) {

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
                ", Gap=" + Gap +
                '}';
    }
}
