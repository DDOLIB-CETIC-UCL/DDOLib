package org.ddolib.ddo.core;

public record SearchStatistics(int nbIterations, int queueMaxSize) {

    @Override
    public String toString() {
        return "SearchStatistics{" +
                "nbIterations=" + nbIterations +
                ", queueMaxSize=" + queueMaxSize +
                '}';
    }
}
