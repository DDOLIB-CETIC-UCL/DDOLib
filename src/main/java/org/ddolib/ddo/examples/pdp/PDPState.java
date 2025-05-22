package org.ddolib.ddo.examples.pdp;

import java.util.BitSet;
import java.util.Objects;

class PDPState {

    //the nodes that we can visit, including
    // all non-visited pick-up nodes
    // all non-visited  delivery nodes such that the related pick-up has been reached
    BitSet openToVisit;

    //every node that has not been visited yet
    BitSet allToVisit;

    //the current node. It is a set because in case of a fusion, we must take the union.
    // However, most of the time, it is a singleton
    BitSet current;

    //the sorted list of all edges that are incident to the current node and to the allToVisitNodes
    //this list might include more edges
    EdgeList sortedEdgeListIncidentToToVisitNodesAndCurrentNode;

    public PDPState(BitSet current, BitSet openToVisit, BitSet allToVisit, EdgeList sortedEdgeListIncidentToToVisitNodesAndCurrentNode) {
        this.openToVisit = openToVisit;
        this.allToVisit = allToVisit;
        this.current = current;
        this.sortedEdgeListIncidentToToVisitNodesAndCurrentNode = sortedEdgeListIncidentToToVisitNodesAndCurrentNode;
    }

    public int hashCode() {
        return Objects.hash(openToVisit, allToVisit, current);
    }

    @Override
    public boolean equals(Object obj) {
        PDPState that = (PDPState) obj;
        if (!that.current.equals(this.current)) return false;
        if (!that.openToVisit.equals(this.openToVisit)) return false;
        return (that.allToVisit.equals(this.allToVisit));
    }

    public PDPState goTo(int node, PDPProblem problem) {
        BitSet newOpenToVisit = (BitSet) openToVisit.clone();
        newOpenToVisit.clear(node);

        BitSet newAllToVisit = (BitSet) allToVisit.clone();
        newAllToVisit.clear(node);

        if (problem.pickupToAssociatedDelivery.containsKey(node)) {
            newOpenToVisit.set(problem.pickupToAssociatedDelivery.get(node));
        }

        if (problem.deliveryToAssociatedPickup.containsKey(node)) {
            int p = problem.deliveryToAssociatedPickup.get(node);
            if (newOpenToVisit.get(p)) {
                newOpenToVisit.clear(p);
            }
        }

        return new PDPState(
                singleton(node),
                newOpenToVisit,
                newAllToVisit,
                sortedEdgeListIncidentToToVisitNodesAndCurrentNode);
    }

    public BitSet singleton(int singletonValue) {
        BitSet toReturn = new BitSet(singletonValue + 1);
        toReturn.set(singletonValue);
        return toReturn;
    }

    public int getSummedLengthOfNSmallestHops(int nbHops, int[][] distance) {

        if (current.cardinality() != 1) throw new Error("no bound for merged");
        int currentNode = current.nextSetBit(0);
        allToVisit.set(currentNode);
        sortedEdgeListIncidentToToVisitNodesAndCurrentNode =
                sortedEdgeListIncidentToToVisitNodesAndCurrentNode.filterUpToLength(nbHops, allToVisit);
        allToVisit.clear(currentNode);

        int total = 0;
        int hopsToDo = nbHops;
        EdgeList current = sortedEdgeListIncidentToToVisitNodesAndCurrentNode;
        while (hopsToDo > 0 && current != null) {
            total += distance[current.nodeA][current.nodeB];
            hopsToDo--;
            current = current.next;
        }
        return total;
    }

    @Override
    public String toString() {
        BitSet closedToVisit = (BitSet) allToVisit.clone();
        closedToVisit.xor(openToVisit);
        if (current.cardinality() != 1) {
            return "PDState(possibleCurrent:" + current + " openToVisit:" + openToVisit + " closedToVisit:" + closedToVisit + ")";
        } else {
            return "PDState(current:" + current.nextSetBit(0) + " openToVisit:" + openToVisit + " closedToVisit:" + closedToVisit + ")";
        }
    }
}
