package org.ddolib.examples.pdptw;

import java.util.BitSet;

/**
 * Compact dominance key for PDPTW states.
 *
 * @param openToVisit currently reachable unvisited nodes
 * @param allToVisit all remaining unvisited nodes
 * @param current current node(s)
 * @param minContent lower bound on vehicle load
 * @param maxContent upper bound on vehicle load
 */
public record PDPTWDominanceKey(BitSet openToVisit,  BitSet allToVisit, BitSet current,
                                int minContent, int maxContent) {
}
