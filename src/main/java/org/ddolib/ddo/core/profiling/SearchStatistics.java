package org.ddolib.ddo.core.profiling;

public record SearchStatistics(int nbIterations, int queueMaxSize, long runTimeMS, SearchStatus SearchStatus, double Gap, double obj) {

    public enum SearchStatus {
        OPTIMAL, UNSAT, SAT, UNKNOWN;
    }
    @Override
    public String toString() {
        return  nbIterations +
                ";" + queueMaxSize +
                "; " + runTimeMS +
                ";" + SearchStatus +
                ";" + Gap +
                ";" + obj
                ;
    }
}
