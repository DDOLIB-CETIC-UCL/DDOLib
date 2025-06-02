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
}



