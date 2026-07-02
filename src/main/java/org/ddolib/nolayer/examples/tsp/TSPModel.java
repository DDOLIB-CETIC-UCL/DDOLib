package org.ddolib.nolayer.examples.tsp;

import org.ddolib.nolayer.modeling.FastLowerBound;
import org.ddolib.nolayer.modeling.Model;
import org.ddolib.nolayer.modeling.Problem;

import java.util.BitSet;

public class TSPModel implements Model<TSPState> {

    private final TSPProblem problem;
    private final FastLowerBound<TSPState> lowerBound;

    public TSPModel(TSPProblem problem) {
        this.problem = problem;

        final double[] leastIncidentEdge = new double[problem.distanceMatrix.length];
        for (int i = 0; i < problem.distanceMatrix.length; i++) {
            double min = Double.POSITIVE_INFINITY;
            for (int j = 0; j < problem.distanceMatrix.length; j++) {
                if (i != j) {
                    min = Math.min(min, problem.distanceMatrix[i][j]);
                }
            }
            leastIncidentEdge[i] = min;
        }

        this.lowerBound = state -> {
            if (state.toVisit.isEmpty() && state.current.get(0)) {
                return 0.0;
            }
            BitSet toVisit = state.toVisit;
            double lb = leastIncidentEdge[0]; // for returning to origin
            for (int i = toVisit.nextSetBit(0); i >= 0; i = toVisit.nextSetBit(i + 1)) {
                lb += leastIncidentEdge[i];
            }
            return lb;
        };
    }

    @Override
    public Problem<TSPState> problem() {
        return problem;
    }

    @Override
    public FastLowerBound<TSPState> lowerBound() {
        return lowerBound;
    }
}
