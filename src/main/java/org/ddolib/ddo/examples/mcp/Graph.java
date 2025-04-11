package org.ddolib.ddo.examples.mcp;

public class Graph {

    private final int[][] adjacencyMatrix;
    public final int numNodes;

    public Graph(int numNodes) {
        this.numNodes = numNodes;
        adjacencyMatrix = new int[numNodes][numNodes];
    }

    public void addEdge(int from, int to, int weight) {
        adjacencyMatrix[from][to] = weight;
        adjacencyMatrix[to][from] = weight;
    }

    public int weightOf(int from, int to) {
        return adjacencyMatrix[from][to];
    }
}
