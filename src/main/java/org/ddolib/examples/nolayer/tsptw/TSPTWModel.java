package org.ddolib.examples.nolayer.tsptw;

import org.ddolib.modeling.nolayer.FastLowerBound;
import org.ddolib.modeling.nolayer.Model;
import org.ddolib.modeling.nolayer.Problem;

import java.util.ArrayList;
import java.util.Collections;

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
    public Problem<TSPTWState> problem() {
        return problem;
    }

    @Override
    public FastLowerBound<TSPTWState> lowerBound() {
        return lowerBound;
    }
}
