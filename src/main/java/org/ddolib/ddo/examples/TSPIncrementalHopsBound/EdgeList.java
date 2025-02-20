package org.ddolib.ddo.examples.TSPIncrementalHopsBound;

import java.util.BitSet;

public class EdgeList {
    final int nodeA;
    final int nodeB;
    EdgeList next;

    public EdgeList(int nodeA, int nodeB, EdgeList next){
        this.nodeA = nodeA;
        this.nodeB = nodeB;
        this.next = next;
    }

    boolean isIncludedIn(BitSet nodes){
        return nodes.get(nodeA) && nodes.get(nodeB);
    }

    EdgeList filterUpToLength(int length, BitSet nodes){
        if(length < 0) return this;
        else if(isIncludedIn(nodes)){
            //keep this
            if(next == null) return this;
            EdgeList newNext = next.filterUpToLength(length-1, nodes);
            if(newNext == next) return this;
            return new EdgeList(nodeA, nodeB, newNext);
        }else{
            //drop this
            if(next == null) return null;
            return next.filterUpToLength(length-1, nodes);
        }
    }

    String string(int nbHops, int[][] matrix){
        if(nbHops <=0) return "";
        String me = "edge(nodeA:" + nodeA + " nodeB:" + nodeB + "dist:" + matrix[nodeA][nodeB] + ")";
        if(next == null && nbHops > 1) return me + "\n tooShortTail";
        if(next != null && nbHops == 1) return me + "\n more, not shown ";
        if(next == null && nbHops == 1) return me + "\n listIsJustCorrectLength";
        else return me + "\n" + next.string(nbHops-1,matrix);
    }
}


