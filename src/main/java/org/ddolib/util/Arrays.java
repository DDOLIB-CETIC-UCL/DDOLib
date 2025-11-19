package org.ddolib.util;

/**
 * Utility class providing common array-related operations.
 * <p>
 * Currently, this class provides functionality to shuffle arrays of integers.
 */
public class Arrays {
    /**
     * Randomly shuffles the elements of the given integer array in place.
     * <p>
     * This method implements the Fisher–Yates (Knuth) shuffle algorithm.
     *
     * @param array the array of integers to be shuffled
     * @throws NullPointerException if the input array is {@code null}
     */
    public static void shuffle(int[] array) {
        for (int i = array.length - 1; i > 0; i--) {
            int j = (int) (Math.random() * (i + 1));
            int temp = array[i];
            array[i] = array[j];
            array[j] = temp;
        }
    }
}
