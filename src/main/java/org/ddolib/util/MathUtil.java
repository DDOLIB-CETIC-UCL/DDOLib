package org.ddolib.util;

public class MathUtil {

    /**
     * Performs a saturated addition (no overflow)
     */
    public static double saturatedAdd(double a, double b) {
        double sum = a + b;
        if (Double.isInfinite(sum)) {
            return sum > 0 ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
        }
        return sum;
    }


    /**
     * Performs a saturated difference (no underflow)
     */
    public static double saturatedDiff(double a, double b) {
        double diff = a - b;
        if (Double.isInfinite(diff)) {
            return diff < 0 ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
        }
        return diff;
    }
}
