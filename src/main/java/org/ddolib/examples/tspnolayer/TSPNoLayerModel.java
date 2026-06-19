package org.ddolib.examples.tspnolayer;

import org.ddolib.examples.tsp.TSPState;
import org.ddolib.modeling.nolayer.NoLayerFastLowerBound;
import org.ddolib.modeling.nolayer.NoLayerModel;
import org.ddolib.modeling.nolayer.NoLayerProblem;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;

public class TSPNoLayerModel implements NoLayerModel<TSPState> {

    private final TSPNoLayerProblem problem;
    private final NoLayerFastLowerBound<TSPState> lowerBound;

    public TSPNoLayerModel(TSPNoLayerProblem problem) {
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
            BitSet toVisit = state.toVisit;
            ArrayList<Double> toVisitLB = new ArrayList<>(toVisit.cardinality() + 1);
            toVisitLB.add(leastIncidentEdge[0]); // for returning to origin
            for (int i = toVisit.nextSetBit(0); i >= 0; i = toVisit.nextSetBit(i + 1)) {
                toVisitLB.add(leastIncidentEdge[i]);
            }
            
            // The number of remaining steps is the number of unvisited nodes + 1 (return to origin)
            // unless we are already at the origin and visited all.
            int remainingSteps = toVisit.cardinality();
            if (!state.current.get(0) || !toVisit.isEmpty()) {
                remainingSteps++; // return to origin
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
    public NoLayerProblem<TSPState> problem() {
        return problem;
    }

    @Override
    public NoLayerFastLowerBound<TSPState> lowerBound() {
        return lowerBound;
    }
}
