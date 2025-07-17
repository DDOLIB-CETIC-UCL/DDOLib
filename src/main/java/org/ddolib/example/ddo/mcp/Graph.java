package org.ddolib.example.ddo.mcp;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Represents a Graph with adjacency matrix. Used for MCP problem
 */
public class Graph {

    private final int[][] adjacencyMatrix;
    public final int numNodes;
    public final int numEdges;

    /**
     * Given an adjacency matrix instantiate a graph. As these class is only used for MCP, we suppose that 2 nodes
     * are not connected if the edge has weight {@code 0}
     *
     * @param adjacencyMatrix The adjacency matrix to initialize the graph.
     */
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


    /**
     * Given two nodes, returns the weight of their edges.
     *
     * @param from The start node of the edge.
     * @param to   The end node of the edges.
     * @return The weight of the edge.
     */
    public int weightOf(int from, int to) {
        return adjacencyMatrix[from][to];
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
