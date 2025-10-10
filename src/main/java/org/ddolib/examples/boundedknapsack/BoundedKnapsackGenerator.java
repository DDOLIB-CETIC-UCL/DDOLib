package org.ddolib.examples.boundedknapsack;

import java.util.Random;

public class BoundedKnapsackGenerator {
    int n; // number of items
    int Range; // data range (100, 1000, or 10000)
    InstanceType type = InstanceType.WEAKLY_CORRELATED; // choose type
    long seed = System.currentTimeMillis();
    public BoundedKnapsackGenerator(int n, int Range, InstanceType type, long seed) {
        this.n = n;
        this.Range = Range;
        this.type = type;
        this.seed = seed;
    }

    public BKSInstance generate() {
        Random rand = new Random(seed);

        int[] values = new int[n];
        int[] weights = new int[n];
        int[] quantity = new int[n];
        int totalWeight = 0;
        for (int j = 0; j < n; j++) {
            int w = 1 + rand.nextInt(Range);
            int p;
            switch (type) {
                case UNCORRELATED:
                    p = 1 + rand.nextInt(Range);
                    break;
                case WEAKLY_CORRELATED:
                    int minP = Math.max(1, w - Range / 10);
                    int maxP = Math.min(Range, w + Range / 10);
                    p = minP + rand.nextInt(maxP - minP + 1);
                    break;
                case STRONGLY_CORRELATED:
                    p = w + 10;
                    break;
                case SUBSET_SUM:
                    p = w;
                    break;
                default:
                    p = 1;
            }
            int m = 5 + rand.nextInt(6);
            values[j] = p;
            weights[j] = w;
            quantity[j] = m;
            totalWeight += w * m;
        }
        int capacity = (int) (0.5 * totalWeight); // capacity = 50% of total
        return new BKSInstance(values, weights, quantity, capacity);
    }
    enum InstanceType {
        UNCORRELATED, // Profit and weight are independent
        WEAKLY_CORRELATED, //Profits roughly follow weights, but not exactly
        STRONGLY_CORRELATED, // Profit = weight + constant
        SUBSET_SUM // Profits exactly equal weights
    }
}
