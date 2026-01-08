package org.ddolib.util;

import java.util.BitSet;

import static java.lang.Math.*;

public class DistanceUtil {

    public static double jaccardDistance(BitSet a, BitSet b) {

        BitSet tmp = (BitSet) a.clone();
        tmp.and(b);
        int intersectionSize = tmp.cardinality();

        tmp = (BitSet) a.clone();
        tmp.or(b);
        int unionSize = tmp.cardinality();

        return (1.0 - ((double) intersectionSize) / unionSize);
    }

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

        return 1 - intersectionSize / unionSize;
    }

    public static double diceDistance(BitSet a, BitSet b) {
        double distance = 0;

        int maxIndex = max(a.length(), b.length());
        for (int i = 0; i < maxIndex; i++) {
            if (a.get(i) && b.get(i)) {
                distance++;
            }
        }
        distance = distance*-2;
        distance = distance / (a.cardinality() + b.cardinality());
        distance += 1;

        return distance;
    }

    public static double hammingDistance(BitSet a, BitSet b) {
        double distance = 0;
        int maxIndex = max(a.length(), b.length());
        for (int i = 0; i < maxIndex; i++) {
            if (a.get(i) != b.get(i)) {
                distance++;
            }
        }

        return distance;
    }

    public static double euclideanDistance(BitSet a, BitSet b) {
        BitSet symmetricDifference = (BitSet) a.clone();
        symmetricDifference.xor(b);
        double dist = symmetricDifference.cardinality();

        return Math.sqrt(dist);
    }

    public static double euclideanDistance(double[]a, double[]b) {
        double distance = 0.0;
        for (int dim = 0; dim < a.length; dim++) {
            distance += pow(a[dim] - b[dim], 2);
        }
        distance = sqrt(distance);

        return distance;
    }

    public static double symmetricDifferenceDistance(BitSet a, BitSet b) {
        BitSet tmp = (BitSet) a.clone();
        tmp.xor(b);
        return tmp.cardinality();
    }


}
