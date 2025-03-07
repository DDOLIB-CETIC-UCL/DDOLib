package org.ddolib.ddo.examples.TSPSmallestAdjacentHopsIncremental;

import java.util.BitSet;

public class SmallestIncidentHopIncremental {
    int baseNode;
    int positionInSortedAdjacents;

    SmallestIncidentHopIncremental next;

    public SmallestIncidentHopIncremental(int baseNode, int positionInSortedAdjacents, SmallestIncidentHopIncremental next){
        this.baseNode = baseNode;
        this.positionInSortedAdjacents = positionInSortedAdjacents;
        this.next = next;
    }

    SmallestIncidentHopIncremental updateToRestrictedNodeSet(BitSet allowedNodes, SortedAdjacents sortedAdjacents){

        if(!allowedNodes.get(baseNode)) {
            //drop me
            return next.updateToRestrictedNodeSet(allowedNodes,sortedAdjacents);
        }else if(allowedNodes.get(sortedAdjacents.sortedAdjacents[baseNode][positionInSortedAdjacents])){
            //keep me as is
            SmallestIncidentHopIncremental newNext = next.updateToRestrictedNodeSet(allowedNodes,sortedAdjacents);
            if(next == newNext) return this;
            else return new SmallestIncidentHopIncremental(baseNode, positionInSortedAdjacents, newNext);
        }else{
            //update me

            int newCurrent = positionInSortedAdjacents+1;
            while(!allowedNodes.get(sortedAdjacents.sortedAdjacents[baseNode][newCurrent])){
                newCurrent++;
            }

            SmallestIncidentHopIncremental newNext = next.updateToRestrictedNodeSet(allowedNodes,sortedAdjacents);
            return new SmallestIncidentHopIncremental(baseNode, newCurrent, newNext);
        }
    }

    int sumOfAllHops(SortedAdjacents sortedAdjacents){
        int toReturn = sortedAdjacents.distanceMatrix[baseNode][sortedAdjacents.sortedAdjacents[baseNode][positionInSortedAdjacents]];
        if(next != null){
            toReturn += next.sumOfAllHops(sortedAdjacents);
        }
        return toReturn;
    }

    int biggestHop(SortedAdjacents sortedAdjacents){
        int thisHop = sortedAdjacents.distanceMatrix[baseNode][sortedAdjacents.sortedAdjacents[baseNode][positionInSortedAdjacents]];
        if(next == null){
            return thisHop;
        }
        int otherHop = next.biggestHop(sortedAdjacents);
        return Math.max(otherHop, thisHop);
    }

    int computeHeuristics(SortedAdjacents sortedAdjacents){
        return sumOfAllHops(sortedAdjacents) - biggestHop(sortedAdjacents);
    }
}


