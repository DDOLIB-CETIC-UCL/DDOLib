package org.ddolib.ddo.examples.pdp;


import java.util.BitSet;

public class SortedEdgeList {
    public final int nodeA;
    public final int nodeB;
    public SortedEdgeList next;

    public SortedEdgeList(int nodeA, int nodeB, SortedEdgeList next){
        this.nodeA = nodeA;
        this.nodeB = nodeB;
        this.next = next;
    }

    boolean isIncludedIn(BitSet nodes){
        return nodes.get(nodeA) && nodes.get(nodeB);
    }

    public SortedEdgeList filterUpToLength(int length, BitSet nodes){
        if(length < 0) return this;
        else if(isIncludedIn(nodes)){
            //keep this
            if(next == null) return this;
            SortedEdgeList newNext = next.filterUpToLength(length-1, nodes);
            if(newNext == next) return this;
            return new SortedEdgeList(nodeA, nodeB, newNext);
        }else{
            //drop this
            if(next == null) return null;
            return next.filterUpToLength(length, nodes);
        }
    }

    public int sumOfFirstHops(int nbHops, int[][] distance){
        int total = 0;
        int hopsToDo = nbHops;
        SortedEdgeList current = this;
        while (hopsToDo > 0 && current != null) {
            total += distance[current.nodeA][current.nodeB];
            hopsToDo--;
            current = current.next;
        }
        return total;
    }
}



