package org.ddolib.ddo.examples.misp;

import org.ddolib.ddo.heuristics.StateRanking;

import java.util.BitSet;

public class MispRanking implements StateRanking<BitSet> {

    @Override
    public int compare(BitSet o1, BitSet o2) {
        return Integer.compare(o1.length(), o2.length());
    }
}