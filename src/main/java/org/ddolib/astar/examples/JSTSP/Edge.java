package org.ddolib.astar.examples.JSTSP;

public class Edge implements Comparable<Edge> {

    private final int v1;
    private final int v2;
    private final double cost;

    public Edge(int v1, int v2, double cost) {
        this.v1 = v1;
        this.v2 = v2;
        this.cost = cost;
    }

    @Override
    public String toString() {
        return "("+v1+" - "+v2+") cost:"+cost;
    }

    @Override
    public int compareTo(Edge o) {
        return Double.compare(this.cost, o.cost);
    }

    public int v1() {
        return v1;
    }

    public int v2() {
        return v2;
    }

    public double cost() {
        return cost;
    }

}
