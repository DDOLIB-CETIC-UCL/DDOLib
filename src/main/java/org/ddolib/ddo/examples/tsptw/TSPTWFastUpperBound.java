package org.ddolib.ddo.examples.tsptw;

import org.ddolib.ddo.heuristics.FastUpperBound;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

import static java.lang.Integer.min;

/**
 * Implementation of a fast upper bound for the TSPTW.
 */
public class TSPTWFastUpperBound implements FastUpperBound<TSPTWState> {

    private static final int INFINITY = Integer.MAX_VALUE;

    private final int numVar;
    private final TSPTWProblem problem;
    private final TSPTWInstance instance;
    private final int[] cheapestEdges;


    public TSPTWFastUpperBound(TSPTWProblem problem) {
        this.problem = problem;
        this.instance = problem.instance;
        this.numVar = instance.distance.length;
        cheapestEdges = precomputeCheapestEdges();
    }

    @Override
    public double fastUpperBound(TSPTWState state, Set<Integer> variables) {
        return -fastLowerBound(state);
    }


    private double fastLowerBound(TSPTWState state) {
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
            backToDepot = min(backToDepot, instance.distance[i][0]);
        }


        if (completeTour > 0) { // There are not enough mustVisit nodes. We complete the tour with the
            // possiblyVisit nodes
            ArrayList<Integer> candidatesToCompleteTour = new ArrayList<>();
            int violation = 0;
            var possiblyIt = state.possiblyVisit().stream().iterator();

            while (possiblyIt.hasNext()) {
                int i = possiblyIt.nextInt();
                candidatesToCompleteTour.add(cheapestEdges[i]);
                backToDepot = min(backToDepot, instance.distance[i][0]);
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
        if (state.time() + total > instance.timeWindows[0].end()) return INFINITY;
        else return total;
    }

    private int[] precomputeCheapestEdges() {
        int[] toReturn = new int[numVar];
        for (int i = 0; i < numVar; i++) {
            int cheapest = INFINITY;
            for (int j = 0; j < numVar; j++) {
                if (j != i) {
                    cheapest = Integer.min(cheapest, instance.distance[i][j]);
                }
            }
            toReturn[i] = cheapest;
        }
        return toReturn;
    }
}
