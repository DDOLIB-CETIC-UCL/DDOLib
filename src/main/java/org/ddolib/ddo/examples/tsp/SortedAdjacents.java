package org.ddolib.ddo.examples.tsp;

import java.util.Comparator;
import java.util.stream.IntStream;

public class SortedAdjacents {

    final int n;
    final int[][] distanceMatrix;

    final int[][] sortedAdjacents;

    public SortedAdjacents(int[][] distanceMatrix) {
        this.distanceMatrix = distanceMatrix;
        this.n = distanceMatrix.length;

        sortedAdjacents = new int[n][];
        for (int i = 0; i < n; i++) {
            int finalI = i;
            sortedAdjacents[i] = IntStream.range(0,n)
                    .filter(j -> j!=finalI)
                    .boxed()
                    .sorted(Comparator.comparing(adj -> distanceMatrix[finalI][adj]))
                    .mapToInt(Integer::intValue).toArray();
        }
    }

    public SmallestIncidentHopIncremental initialHeuristics(){
        SmallestIncidentHopIncremental toReturn = null;
        for (int i = 0; i < n; i++) {
            toReturn = new SmallestIncidentHopIncremental(i, 0, toReturn);
        }
        return toReturn;
    }
}
