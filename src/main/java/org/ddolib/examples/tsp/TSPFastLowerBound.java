package org.ddolib.examples.tsp;

import org.ddolib.modeling.FastLowerBound;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Set;

/**
 * Implementation of a fast lower bound for the Traveling Salesman Problem (TSP).
 *
 * <p>
 * This lower bound estimates the minimum additional cost required to complete a partial tour.
 * For each unvisited node, it considers the smallest incident edge (minimum distance to any other node)
 * and sums these minimum distances. It also includes the starting node (assumed as node 0) to
 * account for the final return to the origin.
 * </p>
 *
 * <p>
 * The bound is not guaranteed to be tight but is computed very efficiently, making it suitable for
 * heuristic or branch-and-bound algorithms where fast estimations are required.
 * </p>
 *
 * <p>
 * Usage:
 * </p>
 * <pre>
 * TSPFastLowerBound lbCalculator = new TSPFastLowerBound(problem);
 * double lb = lbCalculator.fastLowerBound(state, unassignedNodes);
 * </pre>
 */
public class TSPFastLowerBound implements FastLowerBound<TSPState> {
    private final double[] leastIncidentEdge;
    /**
     * Constructs a fast lower bound calculator for the given TSP problem.
     *
     * @param problem The TSP problem instance containing the distance matrix.
     *                It is assumed that the matrix is symmetric and distances are non-negative.
     */
    public TSPFastLowerBound(TSPProblem problem) {
        this.leastIncidentEdge = new double[problem.n];
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
    /**
     * Computes a fast lower bound on the cost to complete the TSP tour from the given state.
     *
     * <p>
     * The bound is computed by summing the smallest incident edges for each unvisited node,
     * including the starting node to account for returning to the origin.
     * </p>
     *
     * @param state               The current state of the tour, containing the set of nodes yet to visit.
     * @param unassignedVariables The set of variables (nodes) not yet assigned in the tour.
     * @return A fast-computed lower bound on the remaining tour cost.
     */
    @Override
    public double fastLowerBound(TSPState state, Set<Integer> unassignedVariables) {
        BitSet toVisit = state.toVisit;
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
        return lb;
    }
}
