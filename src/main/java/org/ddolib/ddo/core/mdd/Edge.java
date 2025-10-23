package org.ddolib.ddo.core.mdd;

import org.ddolib.ddo.core.Decision;

/**
 * Represents an edge in a decision diagram that connects two nodes.
 * <p>
 * Each edge has a source node, an associated decision, and a weight.
 * It is used to represent transitions between nodes in a decision diagram.
 * </p>
 */
final class Edge {
    /** The source node of this edge. */
    public final Node origin;
    /** The decision associated with this edge. */
    public final Decision decision;
    /** The weight of the edge. */
    public double weight;

    /**
     * Creates a new edge connecting a source node with a decision and a weight.
     *
     * @param src the source node of the edge
     * @param d   the decision taken to traverse this edge
     * @param w   the weight of the edge
     */
    public Edge(final Node src, final Decision d, final double w) {
        this.origin = src;
        this.decision = d;
        this.weight = w;
    }
    /**
     * Returns a string representation of this edge, including origin, decision, and weight.
     *
     * @return a string describing the edge
     */
    @Override
    public String toString() {
        return String.format("Origin:%s\n\t Decision:%s\n\t Weight:%s ", origin, decision, weight);
    }
}