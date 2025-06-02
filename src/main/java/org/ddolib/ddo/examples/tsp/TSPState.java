package org.ddolib.ddo.examples.tsp;

import java.util.BitSet;

public class TSPState {

    //every node that has not been visited yet
    final BitSet toVisit;

    //the current node. It is a set because in case of a fusion, we must take the union.
    // However, most of the time, it is a singleton
    final BitSet current;

    SmallestIncidentHopIncremental heuristics;

    public TSPState(BitSet current, BitSet toVisit, SmallestIncidentHopIncremental heuristics) {
        this.toVisit = toVisit;
        this.current = current;
        this.heuristics = heuristics;
    }

    public TSPState goTo(int node) {
        BitSet newToVisit = (BitSet) toVisit.clone();
        newToVisit.clear(node);

        return new TSPState(singleton(node), newToVisit,
                heuristics);
    }

    public BitSet singleton(int singletonValue) {
        BitSet toReturn = new BitSet(singletonValue + 1);
        toReturn.set(singletonValue);
        return toReturn;
    }

    public int getHeuristics(int nbHops, SortedAdjacents sortedAdjacents) {

        BitSet toConsider = (BitSet) toVisit.clone();
        toConsider.or(current);

        //update the heuristics
        heuristics = heuristics.updateToRestrictedNodeSet(toConsider, sortedAdjacents);

        return heuristics.computeHeuristics(sortedAdjacents, nbHops);
    }

    @Override
    public String toString() {
        if (current.cardinality() != 1) {
            return "TSPState(possibleCurrent:" + current + " toVisit:" + toVisit + ")";
        } else {
            return "TSPState(current:" + current.nextSetBit(0) + " toVisit:" + toVisit + ")";
        }
    }
}
