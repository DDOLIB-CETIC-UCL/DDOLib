package org.ddolib.ddo.examples.tsp;

import java.util.BitSet;
import java.util.IntSummaryStatistics;
import java.util.stream.IntStream;

public class SmallestIncidentHopIncremental {
    final int baseNode;
    final int positionInSortedAdjacents;
    final SmallestIncidentHopIncremental next;

    public String thisString(SortedAdjacents sortedAdjacents){
        int adj = sortedAdjacents.sortedAdjacents[baseNode][positionInSortedAdjacents];
        int hop = sortedAdjacents.distanceMatrix[baseNode][adj];
        return "SmallestIncidentHopIncremental(base:" + baseNode + " pos:" + positionInSortedAdjacents + " adj:" +  adj + " hop:" + hop + ")";
    }

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
            if(next == null) return new SmallestIncidentHopIncremental(baseNode, newCurrent, null);
            SmallestIncidentHopIncremental newNext = next.updateToRestrictedNodeSet(allowedNodes,sortedAdjacents);
            return new SmallestIncidentHopIncremental(baseNode, newCurrent, newNext);
        }
    }

    void accumulateHops(IntStream.Builder b, SortedAdjacents sortedAdjacents){
        int thisHop = sortedAdjacents.distanceMatrix[baseNode][sortedAdjacents.sortedAdjacents[baseNode][positionInSortedAdjacents]];
        b.add(thisHop);
        if(next == null){return;}
        next.accumulateHops(b,sortedAdjacents);
    }

    int computeHeuristics(SortedAdjacents sortedAdjacents,int nbHops){
        //build the stream of all hops
        IntStream.Builder b = IntStream.builder();
        accumulateHops(b,sortedAdjacents);

        //TODO this is too slow because of the sort.
        // we do not need a sort actually; only the nbHops smallers
        // and this could be obtained faster than by sorting the whole thing
        IntSummaryStatistics stats = b.build().sorted().limit(nbHops).summaryStatistics();

        //TODO: this returns the smallest adjacent to each node to visit
        //we miss the return at the end however it is not counted in the main either.

        return (int)(stats.getSum() - stats.getMax());
    }
}
