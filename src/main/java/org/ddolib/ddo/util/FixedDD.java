package org.ddolib.ddo.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Random Decision Diagram
 */
public class FixedDD {

    public record Edge(int from, int to, int cost) {
        public Edge(int from, int to, int cost) {
            this.from = from;
            this.to = to;
            this.cost = cost;
        }
    }

    Set<Integer> [] nodes; // nodes on each layer
    Set<Edge> [] edges; // set of edges (from, to, cost) with from and to in two consecutive layers
    int source;
    int sink;
    int [] longestPath; // longest path from each node to the sink, Integer.MIN_VALUE if not reachable

    /**
     * The number of variables is layerSizes + 2
     * The first layer is the source, it has one node
     * The last layer is the sink, it has one node
     */
    public FixedDD(Set<Integer> [] nodes, Set<Edge> [] edges, int source, int sink) {
        this.nodes = nodes;
        this.edges = edges;
        this.source = source;
        this.sink = sink;
        computedLongestPaths();
    }

    public int getSource() {
        return source;
    }

    public int getSink() {
        return sink;
    }

    public int nLayers() {
        return edges.length;
    }

    public int longestPathFromSourceToSink() {
        return longestPath[source];
    }

    private int computedLongestPaths() {
        // solve in DP
        longestPath = new int[sink+1];
        longestPath[sink] = 0;
        for (int n = 0; n < sink; n++) {
            longestPath[n] = Integer.MIN_VALUE;
        }
        for (int l = nodes.length - 2; l >= 0; l--) {
            for (int n: nodes[l]) {
                longestPath[n] = Integer.MIN_VALUE;
                for (Edge e: outEdge(n)) {
                    longestPath[n] = Math.max(longestPath[n], longestPath[e.to()] + e.cost());
                }
            }
        }
        System.out.println(Arrays.toString(longestPath));
        return longestPath[source];
    }

    public Set<Edge> outEdge(int node) {
        Set<Edge> outEdges = new HashSet<>();
        for (Set<Edge> edgeSet : edges) {
            for (Edge edge : edgeSet) {
                if (edge.from() == node) {
                    outEdges.add(edge);
                }
            }
        }
        return outEdges;
    }

    public static FixedDD random(int [] innerLayerSizes, double edgeProba, int minCostEdge, int maxCostEdge, int seed) {
        Random random = new Random(seed);
        // create nodes
        int nodeId = 0;
        int source = nodeId;
        Set<Integer> [] nodes = new Set[innerLayerSizes.length+2];
        nodes[0] = Set.of(nodeId++); // source
        for (int l = 1; l <= innerLayerSizes.length; l++) {
            nodes[l] = new HashSet<>();
            for (int k = 0; k < innerLayerSizes[l-1]; k++) {
                nodes[l].add(nodeId);
                nodeId++;
            }
        }
        int sink = nodeId;
        nodes[nodes.length - 1] = Set.of(sink); // sink
        // create edges
        Set<Edge> [] edges = new Set[innerLayerSizes.length+1];
        // link nodes of consecutive layers randomly with an edge
        for (int l = 0; l < innerLayerSizes.length+1; l++) {
            // create edges between layers i and i+1
            edges[l] = new HashSet<>();
            for (int n: nodes[l]) {
                for (int m: nodes[l+1]) {
                    if (random.nextDouble() < edgeProba) {
                        int cost = (int) (random.nextDouble() * (maxCostEdge - minCostEdge)) + minCostEdge;
                        edges[l].add(new Edge(n, m, cost));
                    }
                }
            }
        }
        return new FixedDD(nodes, edges, source, sink);
    }

    public String toDot() {
        StringBuilder dot = new StringBuilder("digraph RandomDD {\n");

        // Add nodes
        for (Set<Integer> layer : nodes) {
            for (int node : layer) {
                dot.append("    ").append(node).append(" [label=\"").append(node).append("\"];\n");
            }
        }

        // Add edges
        for (Set<Edge> edgeSet : edges) {
            for (Edge edge : edgeSet) {
                dot.append("    ").append(edge.from()).append(" -> ").append(edge.to())
                        .append(" [label=\"").append(edge.cost()).append("\"];\n");
            }
        }

        dot.append("}");
        return dot.toString();
    }

    public void saveToDotFile(String filePath) throws IOException {
        String dotContent = toDot();
        Path path = Paths.get(filePath);
        Files.write(path, dotContent.getBytes());
    }

    public static void main(String[] args) {
        // Example usage
        int[] layerSizes = {4, 7, 4, 8}; // Example layer sizes
        FixedDD randomDD = FixedDD.random(layerSizes, 0.6, 1, 10, 42);
        System.out.println("Longest path from source to sink: " + randomDD.longestPath[randomDD.source]);
        String dotRepresentation = randomDD.toDot();
        System.out.println(dotRepresentation);
        try {
            randomDD.saveToDotFile("random_dd.dot");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }



}
