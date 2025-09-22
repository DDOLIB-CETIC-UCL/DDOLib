package org.ddolib.examples.tsp;

import org.ddolib.modeling.FastLowerBound;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Set;

/**
 * Implementation of a fast upper bound for the TSP.
 */
public class TSPFastLowerBound implements FastLowerBound<TSPState> {
    private final double[] leastIncidentEdge;

    public TSPFastLowerBound(TSPProblem problem) {
        this.leastIncidentEdge = new double[problem.n];
        for (int i = 0; i < problem.n; i++) {
            double min = Double.POSITIVE_INFINITY;
            for (int j = 0; j < problem.n; j++) {
                if (i != j) {
                    min = Math.min(min, problem.distanceMatrix[i][j]);
                }
            }
            leastIncidentEdge[i] = min;
        }
    }

    @Override
    public double fastLowerBound(TSPState state, Set<Integer> unassignedVariables) {
        BitSet toVisit = state.toVisit;
        // for each unvisited node, we take the smallest incident edge
        ArrayList<Double> toVisitLB = new ArrayList<>(unassignedVariables.size());
        toVisitLB.add(leastIncidentEdge[0]); //adding zero for the final come back
        for (int i = toVisit.nextSetBit(0); i >= 0; i = toVisit.nextSetBit(i + 1)) {
            toVisitLB.add(leastIncidentEdge[i]);
        }
        // only unassigned.size() elements are to be visited
        // and there can be fewer than toVisit.size()
        int lb = 0;
        if (toVisitLB.size() > unassignedVariables.size()) {
            Collections.sort(toVisitLB);
        }
        for (int i = 0; i < unassignedVariables.size(); i++) {
            lb += toVisitLB.get(i);
        }
        return -lb;
    }
}
