package org.ddolib.ddo.examples.tsp;

import java.util.Random;

public class TSPInstance {

    /**
     * Generates a random symmetric distance matrix for TSP.
     *
     * @param n the number of nodes
     * @return a symmetric distance matrix of size n x n
     *        where distance[i][j] is the distance between node i and node j
     *        and node i and node j have their coordinate in a 100x100 grid.
     */
    public static int[][] randomMatrix(int n) {
        int[] x = new int[n];
        int[] y = new int[n];
        Random r = new Random(42);
        for (int i = 0; i < n; i++) {
            x[i] = r.nextInt(100);
            y[i] = r.nextInt(100);
        }
        int[][] distance = new int[n][];
        for (int i = 0; i < n; i++) {
            distance[i] = new int[n];
            for (int j = 0; j < n; j++) {
                distance[i][j] = dist(x[i] - x[j], y[i] - y[j]);
            }
        }
        return distance;
    }

    private static int dist(int dx, int dy) {
        return (int) Math.sqrt(dx * dx + dy * dy);
    }
}
