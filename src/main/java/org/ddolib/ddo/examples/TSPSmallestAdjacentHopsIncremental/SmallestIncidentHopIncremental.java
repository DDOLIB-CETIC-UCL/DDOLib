package org.ddolib.ddo.examples.TSPSmallestAdjacentHopsIncremental;

import java.util.BitSet;
import java.util.IntSummaryStatistics;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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
            if(next == null) return null;
            return next.updateToRestrictedNodeSet(allowedNodes,sortedAdjacents);
        }else if(allowedNodes.get(sortedAdjacents.sortedAdjacents[baseNode][positionInSortedAdjacents])){
            //keep me as is
            if(next == null) return this;
            SmallestIncidentHopIncremental newNext = next.updateToRestrictedNodeSet(allowedNodes,sortedAdjacents);
            if(next == newNext) return this;
            else return new SmallestIncidentHopIncremental(baseNode, positionInSortedAdjacents, newNext);
        }else{
            //update me

            int newCurrent = positionInSortedAdjacents+1;
            while(!allowedNodes.get(sortedAdjacents.sortedAdjacents[baseNode][newCurrent])){
                newCurrent++;
            }
            if(next == null) return new SmallestIncidentHopIncremental(baseNode, newCurrent, null);;
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

    void accumulateHops(IntStream.Builder b, SortedAdjacents sortedAdjacents){
        int thisHop = sortedAdjacents.distanceMatrix[baseNode][sortedAdjacents.sortedAdjacents[baseNode][positionInSortedAdjacents]];
        b.add(thisHop);
        if(next == null){return;}
        next.accumulateHops(b,sortedAdjacents);
    }

    int computeHeuristics(SortedAdjacents sortedAdjacents,int nbHops){
        //les nbHops plus petits hops moins le plus grand de ceux là

        //build the stream of all hops
        IntStream.Builder b = IntStream.builder();
        accumulateHops(b,sortedAdjacents);

        IntSummaryStatistics stats = b.build().sorted().limit(nbHops).summaryStatistics();

        return (int)(stats.getSum() - stats.getMax());
    }
}


