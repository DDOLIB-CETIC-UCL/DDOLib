package org.ddolib.ddo.examples.tsp;


import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Relaxation;

import java.util.*;

public class TSPRelax implements Relaxation<TSPState> {

    private final TSPProblem problem;
    private final double[] leastIncidentEdge;

    public TSPRelax(TSPProblem problem) {
        this.problem = problem;
        this.leastIncidentEdge = new double[problem.n];
        for (int i = 0; i < problem.n; i++) {
            double min = Double.MAX_VALUE;
            for (int j = 0; j < problem.n; j++) {
                if (i != j) {
                    min = Math.min(min, problem.distanceMatrix[i][j]);
                }
            }
            leastIncidentEdge[i] = min;
        }
    }

    @Override
    public TSPState mergeStates(final Iterator<TSPState> states) {
        BitSet toVisit = new BitSet(problem.n);
        BitSet current = new BitSet(problem.n);

        while (states.hasNext()) {
            TSPState state = states.next();
            toVisit.or(state.toVisit); // union
            current.or(state.current); // union
        }

        return new TSPState(current, toVisit);
    }

    @Override
    public double relaxEdge(TSPState from, TSPState to, TSPState merged, Decision d, double cost) {
        return cost;
    }

    @Override
    public double fastUpperBound(TSPState state, Set<Integer> unassignedVariables) {
        BitSet toVisit = state.toVisit;
        // for each unvisited node, we take the smallest incident edge
        ArrayList<Double> toVisitLB = new ArrayList<>(unassignedVariables.size());
        for (int i = toVisit.nextSetBit(0); i >= 0; i = toVisit.nextSetBit(i + 1)) {
            toVisitLB.add(leastIncidentEdge[i]);
        }
        // only unassigned.size() elements are to be visited
        int lb = 0;
        Collections.sort(toVisitLB);
        for (int i = 0; i < unassignedVariables.size(); i++) {
            lb += toVisitLB.get(i);
        }
        return -lb;
    }

}

