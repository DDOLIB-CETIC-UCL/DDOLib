package org.ddolib.examples.nolayer.tsptw;

import org.ddolib.nolayer.modeling.FastLowerBound;

/**
 * Fast lower bound for the Traveling Salesman Problem with Time Windows (TSPTW), using
 * the no-layer modeling API.
 * <p>
 * The bound sums, for each city that still must be visited (plus the return to the origin),
 * the least-cost edge incident to that city. It ignores time windows and is therefore a
 * relaxation of the true remaining cost.
 * </p>
 */
public class TSPTWFlb implements FastLowerBound<TSPTWState> {

    private final double[] leastIncidentEdge;

    /**
     * Creates a new fast lower bound for the given TSPTW instance.
     *
     * @param problem the TSPTW instance to bound
     */
    public TSPTWFlb(TSPTWProblem problem) {
        final int n = problem.nbVars;
        leastIncidentEdge = new double[n];
        for (int i = 0; i < n; i++) {
            double min = Double.POSITIVE_INFINITY;
            for (int j = 0; j < n; j++) {
                if (i != j) {
                    min = Math.min(min, problem.distance[i][j]);
                }
            }
            leastIncidentEdge[i] = min;
        }
    }

    @Override
    public double fastLowerBound(TSPTWState state) {
        if (state.mustVisit().isEmpty() && state.currentCity() == 0) {
            return 0.0;
        }
        var toVisit = state.mustVisit();
        double lb = leastIncidentEdge[0]; // for returning to origin
        for (int i = toVisit.nextSetBit(0); i >= 0; i = toVisit.nextSetBit(i + 1)) {
            lb += leastIncidentEdge[i];
        }
        return lb;
    }
}
