package org.ddolib.util;

import java.util.BitSet;

import static java.lang.Math.*;

public class DistanceUtil {

    /**
     * Computes the Jaccard Distance between the two given sets.
     * @param a the first set
     * @param b the second set
     * @return 1 - |a ∩ b| / |a ∪ b|
     */
    public static double jaccardDistance(BitSet a, BitSet b) {

        BitSet tmp = (BitSet) a.clone();
        tmp.and(b);
        int intersectionSize = tmp.cardinality();

        tmp = (BitSet) a.clone();
        tmp.or(b);
        int unionSize = tmp.cardinality();

        return (1.0 - ((double) intersectionSize) / unionSize);
    }

    /**
     * Computes the weighted Jaccard Distance between the two given sets.
     * @param a the first set
     * @param b the second set
     * @param weights the weight of each element in a and b
     * @return the weighted Jaccard Distance between a and b
     */
    public static double weightedJaccardDistance(BitSet a, BitSet b, double[] weights) {
        double intersectionSize =0;
        double unionSize = 0;

        int maxIndex = max(a.length(), b.length());
        for (int i = 0; i < maxIndex; i++) {
            if (a.get(i) || b.get(i)) {
                unionSize += weights[i];
                if (a.get(i) && b.get(i)) {
                    intersectionSize+= weights[i];
                }
            }
        }

        return 1.0 - intersectionSize / unionSize;
    }

    /**
     * Computes the Dice Distance between the two given sets.
     * @param a the first set
     * @param b the second set
     * @return 1 - 2|a ∩ b| / (|a| + |b|)
     */
    public static double diceDistance(BitSet a, BitSet b) {
        BitSet tmp = (BitSet) a.clone();
        tmp.and(b);
        double distance = tmp.cardinality();

        distance = distance*-2;
        distance = distance / (a.cardinality() + b.cardinality());
        distance += 1;

        return distance;
    }

    /**
     * Computes the Euclidean Distance between the two given arrays of coordinates
     * @param a the first array
     * @param b the second array
     * @return the Euclidean distance between a and b
     */
    public static double euclideanDistance(double[]a, double[]b) {
        double distance = 0.0;
        for (int dim = 0; dim < a.length; dim++) {
            distance += pow(a[dim] - b[dim], 2);
        }
        distance = sqrt(distance);

        return distance;
    }

    /**
     * Computes the size of the symmetric difference between a and b
     * @param a the first set
     * @param b the second set
     * @return |a XOR b|
     */
    public static double symmetricDifferenceDistance(BitSet a, BitSet b) {
        BitSet tmp = (BitSet) a.clone();
        tmp.xor(b);
        return tmp.cardinality();
    }

    /**
     * Computes the weighted symmetric difference between a and b
     * @param a the first set
     * @param b the second set
     * @return the weighted symmetric difference between a and b
     */
    public static double weightedSymmetricDifferenceDistance(BitSet a, BitSet b, double[] weights) {
        BitSet tmp = (BitSet) a.clone();
        tmp.xor(b);

        double distance = 0;
        for (int i = tmp.nextSetBit(0); i > 0; i = tmp.nextSetBit(i)) {
            distance += weights[i];
        }

        return distance;
    }
}
