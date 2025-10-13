package org.ddolib.ddo.core.profiling;

import java.util.Arrays;
import java.util.Optional;

public record SearchStatistics(int nbIterations,
                               int queueMaxSize,
                               long runTimeMS,
                               SearchStatus SearchStatus,
                               double Gap,
                               String cacheStats,
                               Optional<Double> currentObjectiveValue,
                               int[] solution,
                               Optional<Double> optimalValue) {

    public SearchStatistics(int nbIterations,
                            int queueMaxSize,
                            long runTimeMS,
                            SearchStatus SearchStatus,
                            double Gap,
                            Optional<Double> currentObjectiveValue,
                            int[] solution,
                            Optional<Double> optimalValue) {
        this(nbIterations, queueMaxSize, runTimeMS, SearchStatus, Gap, "noCache", currentObjectiveValue, solution, optimalValue);
    }

    public enum SearchStatus {
        OPTIMAL, UNSAT, SAT, UNKNOWN;
    }

    @Override
    public String toString() {
        return "\n\t Optimal value : " + optimalValue.map(Object::toString).orElse("No feasible " +
                "solution") +
                "\n\t RunTime (ms) : " + runTimeMS +
                "\n\t SearchStatus : " + SearchStatus +
                "\n\t Gap : " + Gap +
                "\n\t Number of iterations : " + nbIterations +
                "\n\t Queue Max Size : " + queueMaxSize +
                "\n\t Cache Statistics : " + cacheStats +
                "\n\t Current Objective Value : " + currentObjectiveValue.map(Object::toString).orElse("No feasible " +
                "solution") +
                "\n\t Solution : " + Arrays.toString(solution);
    }
}
