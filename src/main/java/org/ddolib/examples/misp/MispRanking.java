package org.ddolib.examples.misp;

import org.ddolib.modeling.StateRanking;

import java.util.BitSet;
/**
 * Implements a ranking strategy for states in the Maximum Independent Set Problem (MISP).
 * <p>
 * The ranking is based on the number of remaining nodes in the state: a state with
 * more remaining nodes is considered more promising for exploration in a decision diagram.
 * </p>
 * <p>
 * This ranking can be used by solvers to prioritize which states to expand first
 * when building or traversing the search space.
 * </p>
 */

public class MispRanking implements StateRanking<BitSet> {
    /**
     * Compares two states based on the number of remaining nodes.
     *
     * @param o1 the first state to compare
     * @param o2 the second state to compare
     * @return a negative integer, zero, or a positive integer as the first state has
     *         fewer, equal, or more remaining nodes than the second state
     */
    @Override
    public int compare(BitSet o1, BitSet o2) {
        // The state with the most remaining nodes is most interesting
        return Integer.compare(o1.cardinality(), o2.cardinality());
    }
}