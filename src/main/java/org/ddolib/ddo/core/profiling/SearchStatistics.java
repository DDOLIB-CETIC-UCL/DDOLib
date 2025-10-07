package org.ddolib.ddo.core.profiling;

import java.util.Arrays;

public record SearchStatistics(int nbIterations, int queueMaxSize, long runTimeMS, SearchStatus SearchStatus, double Gap, String cacheStats, double currentObjectiveValue, int[] solution, double optimalValue) {

    public SearchStatistics(int nbIterations, int queueMaxSize, long runTimeMS, SearchStatus SearchStatus, double Gap, double currentObjectiveValue, int[] solution, double optimalValue) {
        this(nbIterations, queueMaxSize, runTimeMS, SearchStatus, Gap, "noCache", currentObjectiveValue, solution, optimalValue);
    }
    public enum SearchStatus {
        OPTIMAL, UNSAT, SAT, UNKNOWN;
    }
//    @Override
//    public String toString() {
//        return "SearchStatistics{" +
//                "nbIterations=" + nbIterations +
//                ", queueMaxSize=" + queueMaxSize +
//                ", runTimeMS=" + runTimeMS +
//                ", SearchStatus=" + SearchStatus +
//                ", Gap=" + Gap +
//                ", cacheStats=" + cacheStats +
//                ", currentObjectiveValue=" + currentObjectiveValue +
//                ", solution=" + Arrays.toString(solution) +
//                ", optimalValue=" + optimalValue +
//                '}';
//    }
    @Override
    public String toString() {
        return "\n\t Optimal value : " + optimalValue  +
                "\n\t RunTime (ms) : " + runTimeMS +
                "\n\t SearchStatus : " + SearchStatus +
                "\n\t Gap : " + Gap +
                "\n\t Number of iterations : " + nbIterations +
                "\n\t Queue Max Size : " + queueMaxSize +
                "\n\t Cache Statistics : " + cacheStats +
                "\n\t Current Objective Value : " + currentObjectiveValue +
                "\n\t Solution : " + Arrays.toString(solution);
    }
}
