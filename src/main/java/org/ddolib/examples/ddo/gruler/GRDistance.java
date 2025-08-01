package org.ddolib.examples.ddo.gruler;

import smile.math.distance.Distance;

import java.util.BitSet;

public class GRDistance implements Distance<GRState> {

    private int symDiffSize(BitSet a, BitSet b) {
        assert a.length() == b.length();
        int size = 0;
        for (int i = 0; i < a.length(); i++) {
            if (a.get(i) != b.get(i)) {
                size ++;
            }
        }
        return size;
    }

    @Override
    public double d(GRState a, GRState b) {
        int intersectionSizeMarks = symDiffSize(a.getMarks(), b.getMarks());
        int intersectionSizeDistances = symDiffSize(a.getDistances(), b.getDistances());
         
        return intersectionSizeMarks + intersectionSizeDistances + Math.abs(a.getLastMark() - b.getLastMark());
    }
}
