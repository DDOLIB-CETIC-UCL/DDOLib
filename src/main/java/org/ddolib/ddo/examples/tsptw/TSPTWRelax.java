package org.ddolib.ddo.examples.tsptw;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Relaxation;

import java.util.*;

import static java.lang.Integer.min;

public class TSPTWRelax implements Relaxation<TSPTWState> {

    private static final int INFINITY = Integer.MAX_VALUE;

    private final int numVar;
    private final TSPTWProblem problem;
    private final int[] cheapestEdges;

    public TSPTWRelax(TSPTWProblem problem) {
        this.problem = problem;
        this.numVar = problem.nbVars();
        cheapestEdges = precomputeCheapestEdges();
    }

    @Override
    public TSPTWState mergeStates(Iterator<TSPTWState> states) {
        Set<Integer> mergedPos = new HashSet<>();
        int mergedTime = INFINITY;
        BitSet mergedMust = new BitSet(numVar);
        mergedMust.set(0, numVar, true);
        BitSet mergedPossibly = new BitSet(numVar);
        int mergedDepth = 0;
        while (states.hasNext()) {
            TSPTWState current = states.next();
            //The merged position is the union of all the position
            switch (current.position()) {
                case TSPNode(int value) -> mergedPos.add(value);
                case VirtualNodes(Set<Integer> nodes) -> mergedPos.addAll(nodes);
            }
            // The merged must is the intersection of all must set
            mergedMust.and(current.mustVisit());
            // The merged possibly is the union of the all the must sets and all the possibly sets
            mergedPossibly.or(current.mustVisit());
            mergedPossibly.or(current.possiblyVisit());
            // The arrival time of the merged node is the min of all the arrival times
            mergedTime = Integer.min(mergedTime, current.time());
            mergedDepth = current.depth();
        }
        // We exclude the intersection of the must from the merged possibly
        mergedPossibly.andNot(mergedMust);

        return new TSPTWState(new VirtualNodes(mergedPos), mergedTime, mergedMust, mergedPossibly, mergedDepth);
    }

    @Override
    public int relaxEdge(TSPTWState from, TSPTWState to, TSPTWState merged, Decision d, int cost) {
        return cost;
    }

    @Override
    public int fastUpperBound(TSPTWState state, Set<Integer> variables) {
        return -fastLowerBound(state);
    }

    private int fastLowerBound(TSPTWState state) {
        // This lower bound assumes that we will always select the cheapest edges from each node

        int completeTour = numVar - state.depth() - 1;
        //From the current state we go to the closest node
        int start = switch (state.position()) {
            case TSPNode(int value) -> cheapestEdges[value];
            case VirtualNodes(Set<Integer> nodes) -> nodes.stream().mapToInt(x -> cheapestEdges[x]).min().getAsInt();
        };
        int mandatory = 0; // The sum of shortest edges
        int backToDepot = 0; // The shortest edges to the depot


        var mustIt = state.mustVisit().stream().iterator();
        while (mustIt.hasNext()) {
            int i = mustIt.nextInt();
            if (!problem.reachable(state, i)) return INFINITY;
            completeTour--;
            mandatory += cheapestEdges[i];
            backToDepot = min(backToDepot, problem.durationMatrix[i][0]);
        }


        if (completeTour > 0) { // There are not enough mustVisit nodes. We complete the tour with the
            // possiblyVisit nodes
            ArrayList<Integer> candidatesToCompleteTour = new ArrayList<>();
            int violation = 0;
            var possiblyIt = state.possiblyVisit().stream().iterator();

            while (possiblyIt.hasNext()) {
                int i = possiblyIt.nextInt();
                candidatesToCompleteTour.add(i);
                backToDepot = min(backToDepot, problem.durationMatrix[i][0]);
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

    private int[] precomputeCheapestEdges() {
        int[] toReturn = new int[numVar];
        for (int i = 0; i < numVar; i++) {
            int cheapest = INFINITY;
            for (int j = 0; j < numVar; j++) {
                if (j != i) {
                    cheapest = Integer.min(cheapest, problem.durationMatrix[i][j]);
                }
            }
            toReturn[i] = cheapest;
        }
        return toReturn;
    }
}
