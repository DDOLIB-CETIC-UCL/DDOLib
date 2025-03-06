package org.ddolib.ddo.examples.TSPSmallestAdjacentHopsIncremental;

import java.util.AbstractCollection;
import java.util.BitSet;


public class SmallestIncidentHopIncremental {
    int baseNode;
    MList sortedPartiallyFilteredAdjacent;

    SmallestIncidentHopIncremental next;

    public SmallestIncidentHopIncremental(int baseNode, MList sortedPartiallyFilteredAdjacent, SmallestIncidentHopIncremental next){
        this.baseNode = baseNode;
        this.sortedPartiallyFilteredAdjacent = sortedPartiallyFilteredAdjacent;
        this.next = next;
    }

    SmallestIncidentHopIncremental updateToRestrictedNodeSet(BitSet allowedNodes){

        if(!allowedNodes.get(baseNode)) {
            //drop me
            return next.updateToRestrictedNodeSet(allowedNodes);
        }else if(allowedNodes.get(sortedPartiallyFilteredAdjacent.head)){
            //keep me as is
            SmallestIncidentHopIncremental newNext = next.updateToRestrictedNodeSet(allowedNodes);
            if(next == newNext) return this;
            else return new SmallestIncidentHopIncremental(baseNode, sortedPartiallyFilteredAdjacent, newNext);
        }else{
            //update me

            MList newList = sortedPartiallyFilteredAdjacent.tail;
            while(!allowedNodes.get(newList.head)){
                newList = newList.tail;
            }

            SmallestIncidentHopIncremental newNext = next.updateToRestrictedNodeSet(allowedNodes);
            return new SmallestIncidentHopIncremental(baseNode, newList, newNext);
        }
    }

    int sumOfAllHops(int [][] distance){
        if(next == null){
            return distance[baseNode][sortedPartiallyFilteredAdjacent.head];
        }else{
            return distance[baseNode][sortedPartiallyFilteredAdjacent.head] + next.sumOfAllHops(distance);
        }
    }

    int biggestHop(int [][] distance){
        if(next == null){
            return distance[baseNode][closestNeighbour];
        }else{
            return Math.max(distance[baseNode][closestNeighbour],next.sumOfAllHops(distance));
        }
    }

    int computeHeuristics(int [][] distance){
        return sumOfAllHops(distance) - biggestHop(distance);
    }

    String myString(int[][] matrix){
        return "SmallestIncidentHopIncremental(base:" + baseNode + " nearestNeighbour:" + closestNeighbour + " dist:" + matrix[baseNode][closestNeighbour] + ")";
    }

}


