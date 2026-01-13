package org.ddolib.ddo.core.mdd;

/**
 * Enum representing the different types of nodes in the decision diagram.
 * <p>
 * Nodes can be classified into various types to signify their role in the problem-solving process.
 * For example, exact nodes are used for exact solutions, while relaxed nodes are used in relaxed solutions
 * that may not yield an optimal solution but provide bounds or approximations.
 * </p>
 */
public enum NodeType {
    /**
     * Represents an exact node in the decision diagram.
     * This type of node corresponds to an exact solution or a state that contributes to the final solution.
     */
    EXACT,

    /**
     * Represents a relaxed node in the decision diagram.
     * This type of node is part of a relaxed version of the problem, typically used for bounding or approximating solutions.
     */
    RELAXED
}
