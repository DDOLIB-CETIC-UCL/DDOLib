package org.ddolib.ddo.examples.routing.cvrp;

import org.ddolib.ddo.heuristics.StateRanking;

public class CVRPRanking implements StateRanking<CVRPState> {
    @Override
    public int compare(CVRPState o1, CVRPState o2) {
        int smallerIn1 = 0;
        int smallerIn2 = 0;
        for (int vehicle = 0; vehicle < o1.capacity().length; vehicle++) {
            if (o1.capacity()[vehicle] > o2.capacity()[vehicle]) smallerIn2++;
            else smallerIn1++;
        }

        int cmp = Integer.compare(smallerIn1, smallerIn2);
        if (cmp == 0) {
            return Integer.compare(o1.maybeVisit().cardinality(), o2.maybeVisit().cardinality());
        } else {
            return cmp;
        }
    }
}
