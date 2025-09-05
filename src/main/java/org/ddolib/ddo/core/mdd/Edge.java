package org.ddolib.ddo.core.mdd;

import org.ddolib.ddo.core.Decision;

/**
 * This is an edge that connects two nodes from the decision diagram
 */
final class Edge {
    /**
     * The source node of this arc
     */
    public final Node origin;
    /**
     * The decision that was made when traversing this arc
     */
    public final Decision decision;
    /**
     * The weight of the arc
     */
    public double weight;

    /**
     * Creates a new edge between pairs of nodes
     *
     * @param src the source node
     * @param d   the decision that was made when traversing this edge
     * @param w   the weight of the edge
     */
    public Edge(final Node src, final Decision d, final double w) {
        this.origin = src;
        this.decision = d;
        this.weight = w;
    }

    @Override
    public String toString() {
        return String.format("Origin:%s\n\t Decision:%s\n\t Weight:%s ", origin, decision, weight);
    }
}