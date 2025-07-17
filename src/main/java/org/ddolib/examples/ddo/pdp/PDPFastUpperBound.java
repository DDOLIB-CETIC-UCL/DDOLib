package org.ddolib.examples.ddo.pdp;

import org.ddolib.modeling.FastUpperBound;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Set;

/**
 * Implementation of a fast upper bound for the PDP
 */
public class PDPFastUpperBound implements FastUpperBound<PDPState> {
    private final double[] leastIncidentEdge;

    public PDPFastUpperBound(PDPProblem problem) {
        this.leastIncidentEdge = new double[problem.n];
        for (int i = 0; i < problem.n; i++) {
            double min = Double.POSITIVE_INFINITY;
            for (int j = 0; j < problem.n; j++) {
                if (i != j) {
                    min = Math.min(min, problem.instance.distanceMatrix[i][j]);
                }
            }
            leastIncidentEdge[i] = min;
        }
    }

    @Override
    public double fastUpperBound(PDPState state, Set<Integer> variables) {
        BitSet toVisit = state.allToVisit;
        // for each unvisited node, we take the smallest incident edge
        ArrayList<Double> toVisitLB = new ArrayList<>(variables.size());
        toVisitLB.add(leastIncidentEdge[0]); //adding zero for the final come back
        for (int i = toVisit.nextSetBit(0); i >= 0; i = toVisit.nextSetBit(i + 1)) {
            toVisitLB.add(leastIncidentEdge[i]);
        }
        // only unassigned.size() elements are to be visited
        // and there can be fewer than toVisit.size()
        int lb = 0;
        if (toVisitLB.size() > variables.size()) {
            Collections.sort(toVisitLB);
        }
        for (int i = 0; i < variables.size(); i++) {
            lb += toVisitLB.get(i);
        }
        return -lb;
    }
}
