package org.ddolib.ddo.core.profiling;

import java.util.ArrayList;

public record SearchStatistics(int nbIterations, int queueMaxSize, long runTimeMS, SearchStatus SearchStatus, ArrayList<Pair> gaps, double Gap, String cacheStats) {

    public SearchStatistics(int nbIterations, int queueMaxSize, long runTimeMS, SearchStatus SearchStatus, ArrayList<Pair> gaps, double Gap) {
        this(nbIterations, queueMaxSize, runTimeMS, SearchStatus, gaps, Gap, "noCache");
    }
    public enum SearchStatus {
        OPTIMAL, UNSAT, SAT, UNKNOWN;
    }
    @Override
    public String toString() {
        return  nbIterations +
                ";" + queueMaxSize +
                ";" + runTimeMS +
                ";" + SearchStatus +
                ";" + gaps +
                ";" + Gap +
                ";" + cacheStats + ";" ;
    }
}
