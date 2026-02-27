package org.ddolib.common.solver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public record SearchStatistics(
        SearchStatus status,
        int nbIterations,
        int queueMaxSize,
        long runTimeMs,
        double incumbent,
        double gap, int nbSols, int nbSomeNodes, int nbDominatedNodes, ArrayList<int[]> ubs) {

    public String toCSV() {
        return String.format("%s;%d;%d;%d;%f;%f",
                status,
                nbIterations,
                queueMaxSize,
                runTimeMs,
                incumbent,
                gap);
    }

    @Override
    public String toString() {
        // Convert each int[] in ubs to a string like (a,b)
        String ubsStr = ubs.stream()
                .map(arr -> "(" + Arrays.stream(arr)
                        .mapToObj(String::valueOf)
                        .collect(Collectors.joining(",")) + ")")
                .collect(Collectors.joining(", "));

        // Combine all values into a single line, separated by ';'
        return String.join(";",
                String.valueOf(status),
                String.valueOf(nbIterations),
                String.valueOf(queueMaxSize),
                String.valueOf(runTimeMs),
                String.valueOf(incumbent),
                String.valueOf(gap),
                String.valueOf(nbSols),
                String.valueOf(nbSomeNodes),
                String.valueOf(nbDominatedNodes),
                "[" + ubsStr + "]"
        );
    }
}

