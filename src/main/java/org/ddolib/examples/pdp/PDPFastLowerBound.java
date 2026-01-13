package org.ddolib.examples.pdp;

import org.ddolib.modeling.FastLowerBound;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Set;

/**
 * Provides a fast lower bound estimation for the <b>Pickup and Delivery Problem (PDP)</b>.
 * <p>
 * This class implements the {@link FastLowerBound} interface and computes a quick lower
 * bound on the remaining cost of a PDP state. The lower bound is based on the minimum
 * distance of incident edges for each unvisited node, providing a heuristic for pruning
 * suboptimal branches in decision diagram optimization (DDO) or ACS search.
 * </p>
 *
 * <p><b>Key features:</b></p>
 * <ul>
 *     <li>Precomputes the smallest edge incident to each node in the problem instance.</li>
 *     <li>For a given state and a set of variables to assign, sums the smallest edges
 *         associated with the unvisited nodes to estimate a lower bound of the remaining cost.</li>
 *     <li>Includes the starting node (typically node 0) in the estimation to account
 *         for the return or depot cost in PDP instances.</li>
 *     <li>Designed for efficiency: uses precomputed values and simple selection logic
 *         to quickly estimate the bound.</li>
 * </ul>
 *
 * <p>This fast lower bound is particularly useful in search algorithms such as ACS,
 * A*, or DDO to prune infeasible or suboptimal states and accelerate the solution
 * process.</p>
 *
 * @see FastLowerBound
 * @see PDPState
 * @see PDPProblem
 */
public class PDPFastLowerBound implements FastLowerBound<PDPState> {
    /** Precomputed minimum incident edge for each node in the PDP instance. */
    private final double[] leastIncidentEdge;

    /**
     * Constructs a new fast lower bound estimator for a given PDP problem instance.
     *
     * @param problem the PDP problem instance containing the distance matrix
     *                and number of nodes
     */

    public PDPFastLowerBound(PDPProblem problem) {
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
     * Computes a fast lower bound for a given state and a set of unassigned variables.
     *
     * @param state     the current PDP state
     * @param variables the set of variables (nodes) that remain to be assigned
     * @return a lower bound on the cost to complete the PDP from the given state
     */
    @Override
    public double fastLowerBound(PDPState state, Set<Integer> variables) {
        BitSet toVisit = state.allToVisit;
        // for each unvisited node, we take the smallest incident edge
        ArrayList<Double> toVisitLB = new ArrayList<>(variables.size());
        toVisitLB.add(leastIncidentEdge[0]); //adding zero for the final come back
        for (int i = toVisit.nextSetBit(0); i >= 0; i = toVisit.nextSetBit(i + 1)) {
            toVisitLB.add(leastIncidentEdge[i]);
        }
        // only unassigned.size() elements are to be visited
        // and there can be fewer than toVisit.size()
        int lb = 0;
        if (toVisitLB.size() > variables.size()) {
            Collections.sort(toVisitLB);
        }
        for (int i = 0; i < variables.size(); i++) {
            lb += toVisitLB.get(i);
        }
        return lb;
    }
}
