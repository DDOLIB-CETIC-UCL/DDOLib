package org.ddolib.common.frontier;

/**
 * Cutset type for the decision diagram compilation.
 * Enumeration of the different type of cut set of the relax DD
 */
public enum CutSetType {
    /**
     * The cut set corresponds to the last layer of the search space
     * that can be evaluated exactly before approximations are applied.
     */
    LastExactLayer,

    /**
     * The cut set is defined by the current search frontier —
     * the set of nodes that are candidates for expansion.
     */
    Frontier
}
