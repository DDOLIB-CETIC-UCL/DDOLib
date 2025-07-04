package org.ddolib.ddo.examples.pdp;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Relaxation;

import java.util.*;

class PDPRelax implements Relaxation<PDPState> {
    private final PDPProblem problem;
    private final double[] leastIncidentEdge;

    public PDPRelax(PDPProblem problem) {
        this.problem = problem;
        this.leastIncidentEdge = new double[problem.n];
        for (int i = 0; i < problem.n; i++) {
            double min = Double.MAX_VALUE;
            for (int j = 0; j < problem.n; j++) {
                if (i != j) {
                    min = Math.min(min, problem.instance.distanceMatrix[i][j]);
                }
            }
            leastIncidentEdge[i] = min;
        }
    }

    @Override
    public PDPState mergeStates(final Iterator<PDPState> states) {
        //NB: the current node is normally the same in all states
        BitSet openToVisit = new BitSet(problem.n);
        BitSet current = new BitSet(problem.n);
        BitSet allToVisit = new BitSet(problem.n);

        while (states.hasNext()) {
            PDPState state = states.next();
            //take the union; loose precision here
            openToVisit.or(state.openToVisit);
            allToVisit.or(state.allToVisit);
            current.or(state.current);
        }
        //the heuristics is reset to the initial sorted edges and will be filtered again from scratch
        return new PDPState(current, openToVisit, allToVisit);
    }

    @Override
    public double relaxEdge(PDPState from, PDPState to, PDPState merged, Decision d, double cost) {
        return cost;
    }

    @Override
    public double fastUpperBound(PDPState state, Set<Integer> unassignedVariables) {
        BitSet toVisit = state.allToVisit;
        // for each unvisited node, we take the smallest incident edge
        ArrayList<Double> toVisitLB = new ArrayList<>(unassignedVariables.size());
        toVisitLB.add(leastIncidentEdge[0]); //adding zero for the final come back
        for (int i = toVisit.nextSetBit(0); i >= 0; i = toVisit.nextSetBit(i + 1)) {
            toVisitLB.add(leastIncidentEdge[i]);
        }
        // only unassigned.size() elements are to be visited
        // and there can be fewer than toVisit.size()
        int lb = 0;
        if (toVisitLB.size() > unassignedVariables.size()) {
            Collections.sort(toVisitLB);
        }
        for (int i = 0; i < unassignedVariables.size(); i++) {
            lb += toVisitLB.get(i);
        }
        return -lb;
    }
}
