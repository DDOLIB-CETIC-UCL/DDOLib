package org.ddolib.examples.tsptw;

import org.ddolib.modeling.FastLowerBound;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

import static java.lang.Integer.min;

/**
 * Implementation of a fast lower bound for the Traveling Salesman Problem with Time Windows (TSPTW).
 *
 * <p>
 * This class provides a heuristic lower bound on the total tour cost starting from a given {@link TSPTWState}.
 * The lower bound is computed by summing the shortest available edges from the current position,
 * including all mandatory nodes that must be visited and a selection of optional nodes if needed
 * to complete the tour. The bound also considers returning to the depot and respects time window constraints.
 * </p>
 *
 * <p>
 * If any mandatory node is unreachable from the current state, or if completing the tour is impossible
 * within the time windows, the bound returns {@link Integer#MAX_VALUE} to indicate infeasibility.
 * </p>
 *
 * <p>
 * Precomputes the cheapest outgoing edge for each node to speed up repeated lower bound calculations.
 * </p>
 */
public class TSPTWFastLowerBound implements FastLowerBound<TSPTWState> {

    private static final int INFINITY = Integer.MAX_VALUE;

    private final int numVar;
    private final TSPTWProblem problem;
    private final int[] cheapestEdges;

    /**
     * Constructs a fast lower bound calculator for a given TSPTW problem instance.
     *
     * @param problem The TSPTW problem instance.
     */
    public TSPTWFastLowerBound(TSPTWProblem problem) {
        this.problem = problem;
        this.numVar = problem.distance.length;
        cheapestEdges = precomputeCheapestEdges();
    }
    /**
     * Computes a fast lower bound on the remaining tour cost from the given state.
     *
     * <p>
     * The bound includes:
     * </p>
     * <ul>
     *     <li>Distance to the closest next node from the current position</li>
     *     <li>Distance covering all mandatory nodes to be visited</li>
     *     <li>Distance covering optional nodes if necessary to complete the tour</li>
     *     <li>Distance returning to the depot</li>
     * </ul>
     * The calculation respects the time window constraints; if a tour is infeasible, {@code INFINITY} is returned.
     *
     * @param state The current state in the TSPTW problem.
     * @param variables The set of unassigned variables (nodes) to consider for the lower bound.
     * @return A fast lower bound on the tour cost from the current state, or {@link Integer#MAX_VALUE} if infeasible.
     */
    @Override
    public double fastLowerBound(TSPTWState state, Set<Integer> variables) {
        // This lower bound assumes that we will always select the cheapest edges from each node

        int completeTour = numVar - state.depth() - 1;
        //From the current state we go to the closest node
        int start = switch (state.position()) {
            case TSPNode(int value) -> cheapestEdges[value];
            case VirtualNodes(Set<Integer> nodes) ->
                    nodes.stream().mapToInt(x -> cheapestEdges[x]).min().getAsInt();
        };
        int mandatory = 0; // The sum of shortest edges
        int backToDepot = 0; // The shortest edges to the depot


        var mustIt = state.mustVisit().stream().iterator();
        while (mustIt.hasNext()) {
            int i = mustIt.nextInt();
            if (!problem.reachable(state, i)) return INFINITY;
            completeTour--;
            mandatory += cheapestEdges[i];
            backToDepot = min(backToDepot, problem.distance[i][0]);
        }


        if (completeTour > 0) { // There are not enough mustVisit nodes. We complete the tour with the
            // possiblyVisit nodes
            ArrayList<Integer> candidatesToCompleteTour = new ArrayList<>();
            int violation = 0;
            var possiblyIt = state.possiblyVisit().stream().iterator();

            while (possiblyIt.hasNext()) {
                int i = possiblyIt.nextInt();
                candidatesToCompleteTour.add(cheapestEdges[i]);
                backToDepot = min(backToDepot, problem.distance[i][0]);
                if (!problem.reachable(state, i)) violation++;
            }
            if (candidatesToCompleteTour.size() - violation < completeTour) return INFINITY;

            Collections.sort(candidatesToCompleteTour);
            mandatory += candidatesToCompleteTour
                    .subList(0, completeTour)
                    .stream()
                    .mapToInt(x -> x)
                    .sum();
        }

        // No node can be visited. We just need to go back to the depot
        if (mandatory == 0) {
            backToDepot = problem.minDuration(state, 0);
            start = 0;
        }

        int total = start + mandatory + backToDepot;
        if (state.time() + total > problem.timeWindows[0].end()) return INFINITY;
        else return total;
    }
    /**
     * Precomputes the cheapest outgoing edge for each node in the problem.
     *
     * @return An array where {@code cheapestEdges[i]} is the minimum distance from node {@code i} to any other node.
     */
    private int[] precomputeCheapestEdges() {
        int[] toReturn = new int[numVar];
        for (int i = 0; i < numVar; i++) {
            int cheapest = INFINITY;
            for (int j = 0; j < numVar; j++) {
                if (j != i) {
                    cheapest = Integer.min(cheapest, problem.distance[i][j]);
                }
            }
            toReturn[i] = cheapest;
        }
        return toReturn;
    }
}
