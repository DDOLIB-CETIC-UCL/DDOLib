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
}
