package org.ddolib.ddo.examples.TSPKruskal;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Comparator;
import java.util.stream.Stream;

public class Kruskal {
    private final int[][] distance;

    private final int[] nodeToComponentHead;

    private final int n;

    final int maxMerges;
    public final int minimalSpanningTreeWeight;
    final BitSet setsOfNodesToConsider;

    public Kruskal(int[][] distance, BitSet setsOfNodesToConsider, int maxMerges) {
        this.distance = distance;
        this.n = distance.length;
        this.maxMerges = maxMerges;

        this.nodeToComponentHead = new int[n];
        Arrays.setAll(this.nodeToComponentHead, i -> setsOfNodesToConsider.get(i) ? i : -1);

        //System.out.println("kruskal: " + setsOfNodesToConsider + " maxMerges:" + maxMerges);

        this.setsOfNodesToConsider = setsOfNodesToConsider;
        this.minimalSpanningTreeWeight = run();
    }

    private int merge(int nodeA, int nodeB) {
        require(nodeToComponentHead[nodeA] != -1, "err" + nodeA);
        require(nodeToComponentHead[nodeB] != -1, "err" + nodeB);

        int masterA = master(nodeA);
        int masterB = master(nodeB);
        require(nodeToComponentHead[masterA] != -1, "err" + masterA);
        require(nodeToComponentHead[masterB] != -1, "err" + masterB);
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
        return above;
    }

    private static void require(Boolean a, String str) {
        if (!a) throw new Error(str);
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
            require(toReturn != -1, "");
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

        int totalWeight = 0;
        int mergesToDo = Math.min(this.setsOfNodesToConsider.cardinality() - 1, maxMerges);
        for (Edge edge : sortedEdges.toList()) {
            if (master(edge.nodeA) != master(edge.nodeB)) {
                totalWeight += distance[edge.nodeA][edge.nodeB];
                merge(edge.nodeA, edge.nodeB);
                mergesToDo = mergesToDo - 1;
                //System.out.println("merged " + edge.nodeA + " " + edge.nodeB + " dist:" + distance[edge.nodeA][edge.nodeB] + "   mergesToDo:" + mergesToDo);
                if (mergesToDo == 0) {
                    //System.out.println("EarlyStop(" + totalWeight +") ; nodeToComponentHead:" + Arrays.toString(nodeToComponentHead));
                    return totalWeight;
                }
            } else {
                //System.out.println("skipped " + edge.nodeA + " " + edge.nodeB + " dist:" + distance[edge.nodeA][edge.nodeB]);
            }
        }
        //System.out.println("nodeToComponentHead(" + totalWeight +"):" + Arrays.toString(nodeToComponentHead));
        return totalWeight;
    }
}
