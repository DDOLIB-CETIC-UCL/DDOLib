package org.ddolib.util;

/**
 * Utility class for array-related operations
 */

public class Arrays {
    /**
     * This function shuffles an array of integers
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
