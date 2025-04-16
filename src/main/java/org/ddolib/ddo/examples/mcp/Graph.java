package org.ddolib.ddo.examples.mcp;

import java.util.Arrays;
import java.util.stream.Collectors;

public class Graph {

    private final int[][] adjacencyMatrix;
    public final int numNodes;
    public final int numEdges;

    public Graph(int numNodes) {
        this.numNodes = numNodes;
        adjacencyMatrix = new int[numNodes][numNodes];
        numEdges = Arrays.stream(adjacencyMatrix)
                .map(row -> (int) Arrays.stream(row)
                        .filter(x -> x != 0)
                        .count()
                )
                .reduce(0, Integer::sum);
    }

    public Graph(int[][] adjacencyMatrix) {
        this.adjacencyMatrix = adjacencyMatrix;
        this.numNodes = adjacencyMatrix.length;
        numEdges = Arrays.stream(adjacencyMatrix)
                .map(row -> (int) Arrays.stream(row)
                        .filter(x -> x != 0)
                        .count()
                )
                .reduce(0, Integer::sum) / 2;
    }

    public void addEdge(int from, int to, int weight) {
        adjacencyMatrix[from][to] = weight;
        adjacencyMatrix[to][from] = weight;
    }

    public int weightOf(int from, int to) {
        return adjacencyMatrix[from][to];
    }

    public int degree(int node) {
        return (int) Arrays.stream(adjacencyMatrix[node]).filter(value -> value != 0).count();
    }

    @Override
    public String toString() {
        return Arrays.stream(adjacencyMatrix)
                .map(row -> Arrays.stream(row)
                        .mapToObj(x -> String.format("%2s", x))
                        .collect(Collectors.joining(" ")))
                .collect(Collectors.joining("\n"));
    }
}
