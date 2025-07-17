package org.ddolib.example.ddo.misp;

import org.ddolib.modeling.StateRanking;

import java.util.BitSet;

public class MispRanking implements StateRanking<BitSet> {

    @Override
    public int compare(BitSet o1, BitSet o2) {
        // The state with the most remaining nodes is most interesting
        return Integer.compare(o1.cardinality(), o2.cardinality());
    }
}