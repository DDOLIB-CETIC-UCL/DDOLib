package org.ddolib.ddo.core.mdd;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an atomic node in a decision diagram.
 * <p>
 * By itself, a {@code Node} does not hold much interpretable information, but it serves as a building block
 * for constructing decision diagrams. It stores edges, values, and auxiliary information used in
 * dynamic programming, search, or pruning operations.
 * </p>
 */
final class Node {
    /** The length of the longest path to this node. */
    public double value;

    /** The length of the longest suffix of this node (used in local bound calculations). */
    public Double suffix;

    /** The edge terminating the longest path to this node. */
    public Edge best;

    /** The list of edges leading to this node. */
    public List<Edge> edges;

    /** The type of this node (e.g., exact, relaxed). */
    public NodeType type;

    /** Flag indicating if the node is marked. */
    public boolean isMarked;

    /** Overapproximation of the shortest path from this node to a terminal node. */
    public double flb = Double.NEGATIVE_INFINITY;

    // Fields used if working with a cache

    /** Flag indicating if the node is in the exact cutset. */
    public boolean isInExactCutSet = false;

    /** Flag indicating if the node is above the exact cutset. */
    public boolean isAboveExactCutSet = false;


    /**
     * Creates a new {@code Node} with the given value.
     * <p>
     * Initializes suffix and best edge to {@code null}, creates an empty list of edges,
     * sets the type to {@link NodeType#EXACT}, and marks the node as unmarked.
     * </p>
     *
     * @param value the initial value of the node (length of the longest path to this node)
     */
    public Node(final double value) {
        this.value = value;
        this.suffix = null;
        this.best = null;
        this.edges = new ArrayList<>();
        this.type = NodeType.EXACT;
        this.isMarked = false;
    }

    /**
     * Returns a string representation of this node, including its value, suffix, best edge, and parent edges.
     *
     * @return a string describing the node
     */
    @Override
    public String toString() {
        return String.format("Node: value:%.0f - suffix: %s - best edge: %s - parent edges: %s",
                value, suffix, best, edges);
    }

    // Deterministic hash
    private static int nextHash = 0;
    private final int hash = nextHash++;

    @Override
    public int hashCode() {
        return hash;
    }
}