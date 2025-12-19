package org.ddolib.util;
/**
 * Utility class providing mathematical operations with saturation semantics.
 * <p>
 * Saturated operations ensure that results do not overflow or underflow,
 * returning {@link Double#POSITIVE_INFINITY} or {@link Double#NEGATIVE_INFINITY}
 * instead of exceeding the representable range of a double.
 */
public class MathUtil {

    /**
     * Performs a saturated addition of two double values.
     * <p>
     * If the addition overflows the maximum representable double, the result
     * is {@link Double#POSITIVE_INFINITY}. If it underflows below the minimum
     * representable double, the result is {@link Double#NEGATIVE_INFINITY}.
     *
     * @param a the first operand
     * @param b the second operand
     * @return the saturated sum of {@code a} and {@code b}
     */
    public static double saturatedAdd(double a, double b) {
        double sum = a + b;
        if (Double.isInfinite(sum)) {
            return sum > 0 ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
        }
        return sum;
    }


    /**
     * Performs a saturated subtraction of two double values.
     * <p>
     * If the subtraction overflows the maximum representable double, the result
     * is {@link Double#POSITIVE_INFINITY}. If it underflows below the minimum
     * representable double, the result is {@link Double#NEGATIVE_INFINITY}.
     *
     * @param a the minuend
     * @param b the subtrahend
     * @return the saturated difference {@code a - b}
     */
    public static double saturatedDiff(double a, double b) {
        double diff = a - b;
        if (Double.isInfinite(diff)) {
            return diff < 0 ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
        }
        return diff;
    }
}
