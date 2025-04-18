package org.ddolib.ddo.examples.TSPKruskal;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class Kruskal {
    private final int[][] distance;

    private final int[] nodeToComponentHead;

    final int maxMerges;
    public final int minimalSpanningTreeWeight;
    final BitSet setsOfNodesToConsider;

    public Kruskal(int[][] distance, BitSet setsOfNodesToConsider, int maxMerges) {
        this.distance = distance;
        int n = distance.length;
        this.maxMerges = maxMerges;

        this.nodeToComponentHead = new int[n];
        Arrays.setAll(this.nodeToComponentHead, i -> setsOfNodesToConsider.get(i) ? i : -1);

        this.setsOfNodesToConsider = setsOfNodesToConsider;
        this.minimalSpanningTreeWeight = run();
    }

    private void merge(int nodeA, int nodeB) {
        int masterA = master(nodeA);
        int masterB = master(nodeB);

        int above;
        int below;
        if (masterA > masterB) {
            above = masterA;
            below = masterB;
        } else {
            above = masterB;
            below = masterA;
        }
        nodeToComponentHead[below] = above;
    }

    static class Edge {
        final int nodeA;
        final int nodeB;

        public Edge(int nodeA, int nodeB) {
            this.nodeA = nodeA;
            this.nodeB = nodeB;
        }
    }

    private int master(int node) {
        if (nodeToComponentHead[node] == node) {
            return node;
        } else {
            int toReturn = master(nodeToComponentHead[node]);
            nodeToComponentHead[node] = toReturn;
            return toReturn;
        }
    }

    private int run() {
        Stream<Edge> sortedEdges = this.setsOfNodesToConsider.stream().boxed().flatMap(
                        node1 ->
                                this.setsOfNodesToConsider
                                        .stream()
                                        .filter(node2 -> node1 < node2)
                                        .boxed()
                                        .map(node2 -> new Edge(node1, node2))
                )
                .sorted(Comparator.comparing(e -> distance[e.nodeA][e.nodeB]));

        AtomicInteger totalWeight = new AtomicInteger();
        AtomicInteger mergesToDo = new AtomicInteger(Math.min(this.setsOfNodesToConsider.cardinality() - 1, maxMerges));
        sortedEdges.anyMatch(edge -> {
            if (master(edge.nodeA) != master(edge.nodeB)) {
                totalWeight.addAndGet(distance[edge.nodeA][edge.nodeB]);
                merge(edge.nodeA, edge.nodeB);
                mergesToDo.set(mergesToDo.get() - 1);
            }
            return mergesToDo.get() == 0;
        });
        return totalWeight.get();
    }
}
