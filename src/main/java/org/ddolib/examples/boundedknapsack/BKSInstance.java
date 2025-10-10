package org.ddolib.examples.boundedknapsack;

import java.util.Arrays;

public class BKSInstance {
    int capacity;
    int[] values;
    int[] weights;
    int[] quantity;
    public BKSInstance(int[] values, int[] weights, int[] quantity, int capacity) {
        this.values = values;
        this.weights = weights;
        this.quantity = quantity;
        this.capacity = capacity;
    }
    @Override
    public String toString() {
        return"(" + " values " + Arrays.toString(values) + " weights " + Arrays.toString(weights) + " quantity " + Arrays.toString(quantity) + " capacity " + capacity + ")";
    }
}
