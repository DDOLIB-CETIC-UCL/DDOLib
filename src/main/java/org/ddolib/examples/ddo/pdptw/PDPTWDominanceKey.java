package org.ddolib.examples.ddo.pdptw;

import java.util.BitSet;

public record PDPTWDominanceKey(BitSet openToVisit,  BitSet allToVisit, BitSet current,
                                int minContent, int maxContent) {
}
