package org.ddolib.examples.nolayer.tsp;

import org.ddolib.nolayer.modeling.FastLowerBound;

import java.util.BitSet;

public class TSPFlb implements FastLowerBound<TSPState> {
    private final double[] leastIncidentEdge;

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
