package org.ddolib.ddo.examples.tsptw;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Problem;

import java.util.*;

public class TSPTWProblem implements Problem<TSPTWState> {

    final int[][] timeMatrix;
    final TimeWindow[] timeWindows;

    public TSPTWProblem(int[][] timeMatrix, TimeWindow[] timeWindows) {
        this.timeMatrix = timeMatrix;
        this.timeWindows = timeWindows;
    }

    @Override
    public int nbVars() {
        return timeMatrix.length;
    }

    @Override
    public TSPTWState initialState() {
        BitSet must = new BitSet(nbVars());
        must.set(1, nbVars(), true);
        BitSet might = new BitSet(nbVars());
        return new TSPTWState(new TSPNode(0), 0, must, might, 0);
    }

    @Override
    public int initialValue() {
        return 0;
    }

    @Override
    public Iterator<Integer> domain(TSPTWState state, int var) {
        BitSet toReturn = new BitSet(state.mustVisit().length());
        if (state.depth() == nbVars() - 1) {
            //The only decision for the last variable is to go back to the depot
            toReturn.set(0, reachable(state, 0));
        } else {

            var mustIt = state.mustVisit().stream().iterator();
            while (mustIt.hasNext()) {
                int i = mustIt.nextInt();
                toReturn.set(i, reachable(state, i));
            }

            if (state.mustVisit().length() < nbVars() - state.depth()) {
                var possiblyIt = state.possiblyVisit().stream().iterator();
                while (possiblyIt.hasNext()) {
                    int i = possiblyIt.nextInt();
                    toReturn.set(i, reachable(state, i));
                }
            }
        }
        return toReturn.stream().iterator();
    }

    @Override
    public TSPTWState transition(TSPTWState state, Decision decision) {
        int target = decision.val();
        TSPNode newPos = new TSPNode(target);
        int newTime = arrivalTime(state, target);
        BitSet newMust = (BitSet) state.mustVisit().clone();
        newMust.set(target, false);
        BitSet newMight = (BitSet) state.possiblyVisit().clone();
        newMight.set(target, false);
        return new TSPTWState(newPos, newTime, newMust, newMight, state.depth() + 1);
    }

    @Override
    public int transitionCost(TSPTWState state, Decision decision) {
        int to = decision.val();

        int travel = minDistance(state, to);
        int arrival = state.time() + travel;
        int waiting = arrival < timeWindows[to].start() ? timeWindows[to].start() - arrival : 0;
        return -(travel + waiting);

    }

    boolean reachable(TSPTWState state, Integer target) {
        int duration = minDistance(state, target);
        return state.time() + duration <= timeWindows[target].end();
    }

    int minDistance(TSPTWState from, Integer to) {
        return switch (from.position()) {
            case TSPNode(int value) -> timeMatrix[value][to];
            case Virtual(Set<Integer> nodes) -> nodes.stream().mapToInt(x -> timeMatrix[x][to]).min().getAsInt();
        };
    }

    int arrivalTime(TSPTWState from, Integer to) {
        int time = from.time() + minDistance(from, to);
        return Integer.max(time, timeWindows[to].start());

    }

}
