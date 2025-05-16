package org.ddolib.ddo.util;

/**
 * This class applies the function shuffle to an array
 */

public class Arrays {

    public static void shuffle(int [] array) {
        for (int i = array.length - 1; i > 0; i--) {
            int j = (int) (Math.random() * (i + 1));
            int temp = array[i];
            array[i] = array[j];
            array[j] = temp;
        }
    }
}
