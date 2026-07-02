package org.ddolib.nolayer.examples.tsptw;

import org.ddolib.nolayer.modeling.FastLowerBound;

public class TSPTWFlb implements FastLowerBound<TSPTWState> {

    private final double[] leastIncidentEdge;

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
