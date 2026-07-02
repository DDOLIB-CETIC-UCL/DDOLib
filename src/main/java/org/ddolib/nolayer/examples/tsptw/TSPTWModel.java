package org.ddolib.nolayer.examples.tsptw;

import org.ddolib.nolayer.modeling.FastLowerBound;
import org.ddolib.nolayer.modeling.Model;
import org.ddolib.nolayer.modeling.Problem;

public class TSPTWModel implements Model<TSPTWState> {

    private final TSPTWProblem problem;
    private final FastLowerBound<TSPTWState> lowerBound;

    public TSPTWModel(TSPTWProblem problem) {
        this.problem = problem;

        final int n = problem.nbVars;
        final double[] leastIncidentEdge = new double[n];
        for (int i = 0; i < n; i++) {
            double min = Double.POSITIVE_INFINITY;
            for (int j = 0; j < n; j++) {
                if (i != j) {
                    min = Math.min(min, problem.distance[i][j]);
                }
            }
            leastIncidentEdge[i] = min;
        }

        this.lowerBound = state -> {
            if (state.mustVisit().isEmpty() && state.currentCity() == 0) {
                return 0.0;
            }
            var toVisit = state.mustVisit();
            double lb = leastIncidentEdge[0]; // for returning to origin
            for (int i = toVisit.nextSetBit(0); i >= 0; i = toVisit.nextSetBit(i + 1)) {
                lb += leastIncidentEdge[i];
            }
            return lb;
        };
    }

    @Override
    public Problem<TSPTWState> problem() {
        return problem;
    }

    @Override
    public FastLowerBound<TSPTWState> lowerBound() {
        return lowerBound;
    }
}
