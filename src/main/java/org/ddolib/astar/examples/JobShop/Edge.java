package org.ddolib.astar.examples.JobShop;

public class Edge {
    public int to;
    public int weight;

    public Edge(int to, int weight) {
        this.to = to;
        this.weight = weight;
    }
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Edge) {
            Edge e = (Edge) obj;
            return to == e.to && weight == e.weight;
        }
        return false;
    }
}
