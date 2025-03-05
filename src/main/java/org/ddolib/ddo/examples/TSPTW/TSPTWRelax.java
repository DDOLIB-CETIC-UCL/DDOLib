package org.ddolib.ddo.examples.TSPTW;

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
            switch (current.position()) {
                case TSPNode(int value) -> mergedPos.add(value);
                case Virtual(Set<Integer> nodes) -> mergedPos.addAll(nodes);
            }
            mergedMust.and(current.mustVisit());
            mergedPossibly.or(current.mustVisit());
            mergedPossibly.or(current.possiblyVisit());
            mergedTime = Integer.min(mergedTime, current.time());
            mergedDepth = current.depth();
        }
        mergedPossibly.andNot(mergedMust);

        return new TSPTWState(new Virtual(mergedPos), mergedTime, mergedMust, mergedPossibly, mergedDepth);
    }

    @Override
    public int relaxEdge(TSPTWState from, TSPTWState to, TSPTWState merged, Decision d, int cost) {
        return cost;
    }

    @Override
    public int fastUpperBound(TSPTWState state, Set<Integer> variables) {
        return -fastLowerBound(state, variables);
    }

    private int fastLowerBound(TSPTWState state, Set<Integer> variables) {
        int completeTour = numVar - state.depth() - 1;
        //From the current we go to the closest node
        int start = switch (state.position()) {
            case TSPNode(int value) -> cheapestEdges[value];
            case Virtual(Set<Integer> nodes) -> nodes.stream().mapToInt(x -> cheapestEdges[x]).min().getAsInt();
        };
        // The sum of shortest edges
        int mandatory = 0;
        int backToDepot = 0;


        var mustIt = state.mustVisit().stream().iterator();
        while (mustIt.hasNext()) {
            int i = mustIt.nextInt();
            if (!problem.reachable(state, i)) return INFINITY;
            completeTour--;
            mandatory += cheapestEdges[i];
            backToDepot = min(backToDepot, problem.timeMatrix[i][0]);
        }

        ArrayList<Integer> tmp = new ArrayList<>();
        int violation = 0;
        var possiblyIt = state.possiblyVisit().stream().iterator();
        while (possiblyIt.hasNext()) {
            int i = possiblyIt.nextInt();
            tmp.add(i);
            backToDepot = min(backToDepot, problem.timeMatrix[i][0]);
            if (!problem.reachable(state, i)) violation++;
        }
        if (tmp.size() - violation < completeTour) return INFINITY;

        Collections.sort(tmp);
        mandatory += tmp.subList(0, completeTour).stream().mapToInt(x -> x).sum();

        // No node can be visited. We just need to go back to the depot
        if (mandatory == 0) {
            backToDepot = problem.minDistance(state, 0);
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
                    cheapest = Integer.min(cheapest, problem.timeMatrix[i][j]);
                }
            }
            toReturn[i] = cheapest;
        }
        return toReturn;
    }
}
