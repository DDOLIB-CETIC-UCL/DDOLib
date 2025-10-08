package org.ddolib.ddo.core.mdd;

import java.util.ArrayList;
import java.util.List;

/**
 * This is an atomic node from the decision diagram. Per-se, it does not
 * hold much interpretable information.
 */
final class Node {
    /**
     * The length of the longest path to this node
     */
    public double value;
    /**
     * The length of the longest suffix of this node (bottom part of a local bound)
     */
    public Double suffix;
    /**
     * The edge terminating the longest path to this node
     */
    public Edge best;
    /**
     * The list of edges leading to this node
     */
    public List<Edge> edges;

    /**
     * The type of this node (exact, relaxed, etc...)
     */
    public NodeType type;

    /**
     * The falg to indicate if a node is marked
     */
    public boolean isMarked;

    /**
     * An overapproximation of the shortest from this node to a terminal node
     */
    public double flb = Double.NEGATIVE_INFINITY;

    // USED IF WORKING WITH CACHE

    /**
     * The flag to indicate if a node is in exact cutset
     */
    public boolean isInExactCutSet = false;

    /**
     * The flag to indicate if a node is above the exact cutset
     */
    public boolean isAboveExactCutSet = false;


    /**
     * Creates a new node
     */
    public Node(final double value) {
        this.value = value;
        this.suffix = null;
        this.best = null;
        this.edges = new ArrayList<>();
        this.type = NodeType.EXACT;
        this.isMarked = false;
    }


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