package org.ddolib.examples.tsptwnolayer;

import org.ddolib.modeling.nolayer.NoLayerFastLowerBound;
import org.ddolib.modeling.nolayer.NoLayerModel;
import org.ddolib.modeling.nolayer.NoLayerProblem;

import java.util.ArrayList;
import java.util.Collections;

public class TSPTWNoLayerModel implements NoLayerModel<TSPTWNoLayerState> {

    private final TSPTWNoLayerProblem problem;
    private final NoLayerFastLowerBound<TSPTWNoLayerState> lowerBound;

    public TSPTWNoLayerModel(TSPTWNoLayerProblem problem) {
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
            var toVisit = state.mustVisit();
            ArrayList<Double> toVisitLB = new ArrayList<>(toVisit.cardinality() + 1);
            toVisitLB.add(leastIncidentEdge[0]); // for returning to origin
            for (int i = toVisit.nextSetBit(0); i >= 0; i = toVisit.nextSetBit(i + 1)) {
                toVisitLB.add(leastIncidentEdge[i]);
            }

            int remainingSteps = toVisit.cardinality();
            if (state.currentCity() != 0 || !toVisit.isEmpty()) {
                remainingSteps++;
            }

            int lb = 0;
            if (toVisitLB.size() > remainingSteps) {
                Collections.sort(toVisitLB);
            }
            for (int i = 0; i < remainingSteps && i < toVisitLB.size(); i++) {
                lb += toVisitLB.get(i);
            }
            return lb;
        };
    }

    @Override
    public NoLayerProblem<TSPTWNoLayerState> problem() {
        return problem;
    }

    @Override
    public NoLayerFastLowerBound<TSPTWNoLayerState> lowerBound() {
        return lowerBound;
    }
}
