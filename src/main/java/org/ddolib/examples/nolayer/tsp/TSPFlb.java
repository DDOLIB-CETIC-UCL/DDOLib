package org.ddolib.examples.nolayer.tsp;

import org.ddolib.nolayer.modeling.FastLowerBound;

import java.util.BitSet;

/**
 * Fast lower bound for the Traveling Salesman Problem (TSP), using the no-layer modeling API.
 * <p>
 * The bound sums, for each city that still must be visited (plus the return to the origin),
 * the least-cost edge incident to that city.
 * </p>
 */
public class TSPFlb implements FastLowerBound<TSPState> {
    private final double[] leastIncidentEdge;

    /**
     * Creates a new fast lower bound for the given TSP instance.
     *
     * @param problem the TSP instance to bound
     */
    public TSPFlb(TSPProblem problem) {
        leastIncidentEdge = new double[problem.n];
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
    public double fastLowerBound(TSPState state) {
        if (state.toVisit.isEmpty() && state.current.get(0)) {
            return 0.0;
        }
        BitSet toVisit = state.toVisit;
        double lb = leastIncidentEdge[0];  // for returning to origin
        for (int i = toVisit.nextSetBit(0); i >= 0; i = toVisit.nextSetBit(i + 1)) {
            lb += leastIncidentEdge[i];

        }
        return lb;
    }
}
