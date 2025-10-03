package org.ddolib.examples.pigmentscheduling;

import org.ddolib.util.io.InputReader;

import java.util.Arrays;

public class PSInstance {

    public int nItems;
    public int horizon;
    // dim = nItems
    public int[] stockingCost; // cost of stocking item i

    // dim = nItems x nItems
    public int[][] changeoverCost; // cost of changing from item i to item j

    // dim = nItems x (horizon+1)
    public int[][] previousDemands; // previousDemands[i][t] = the largest time slot < t where a demand for item i occurs

    // dim = nItems x horizon
    public int[][] remainingDemands; // remainingDemands[i][t] = the total demand for item i on [0..t]

    public int optimal; // optimal objective value

    public PSInstance(String path) {

        InputReader reader = new InputReader(path);
        horizon = reader.getInt();
        nItems = reader.getInt();
        int nOrders = reader.getInt();

        changeoverCost = new int[nItems][nItems];
        for (int i = 0; i < nItems; i++) {
            for (int j = 0; j < nItems; j++) {
                changeoverCost[i][j] = reader.getInt();
            }
        }

        stockingCost = new int[nItems];
        for (int i = 0; i < nItems; i++) {
            stockingCost[i] = reader.getInt();
        }

        int[][] demands = new int[nItems][horizon];
        for (int i = 0; i < nItems; i++) {
            for (int j = 0; j < horizon; j++) {
                demands[i][j] = reader.getInt();
            }
        }

        optimal = reader.getInt();

        previousDemands = new int[nItems][horizon + 1];
        for (int i = 0; i < nItems; i++) {
            Arrays.fill(previousDemands[i], -1);
        }
        for (int t = 1; t <= horizon; t++) {
            for (int i = 0; i < nItems; i++) {
                if (demands[i][t - 1] > 0) {
                    previousDemands[i][t] = t - 1;
                } else {
                    previousDemands[i][t] = previousDemands[i][t - 1];
                }
            }
        }
        remainingDemands = new int[nItems][horizon];
        for (int i = 0; i < nItems; i++) {
            for (int t = 0; t < horizon; t++) {
                if (t == 0) {
                    remainingDemands[i][t] = demands[i][t];
                } else {
                    remainingDemands[i][t] = remainingDemands[i][t - 1] + demands[i][t];
                }
            }
        }
    }
}
