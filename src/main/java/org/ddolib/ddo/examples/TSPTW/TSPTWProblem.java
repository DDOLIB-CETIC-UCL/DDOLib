package org.ddolib.ddo.examples.TSPTW;

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
        
        if (state.timeElapsed()) return Collections.emptyIterator();
        else if (state.depth() == nbVars() - 1) return List.of(0).iterator();
        else {
            BitSet toReturn = (BitSet) state.mustVisit().clone();
            if (state.mustVisit().length() < nbVars() - state.depth()) toReturn.or(state.mightVisit());
            return toReturn.stream().iterator();
        }
    }

    @Override
    public TSPTWState transition(TSPTWState state, Decision decision) {
        int target = decision.val();
        TSPNode newPos = new TSPNode(target);
        int newTime = arrivalTime(state, target);
        BitSet newMust = (BitSet) state.mustVisit().clone();
        newMust.set(target, false);
        BitSet newMight = (BitSet) state.mightVisit().clone();
        newMight.set(target, false);
        boolean elapsed = !reachable(state, target);
        return new TSPTWState(newPos, newTime, newMust, newMight, state.depth() + 1, elapsed);
    }

    @Override
    public int transitionCost(TSPTWState state, Decision decision) {
        int to = decision.var();
        if (reachable(state, to)) {
            int travel = minDistance(state, to);
            int arrival = state.time() + travel;
            int waiting = arrival < timeWindows[to].start() ? timeWindows[to].start() - arrival : 0;
            return -(travel + waiting);
        } else {
            return -Integer.MAX_VALUE;
        }
    }

    private boolean reachable(TSPTWState state, Integer target) {
        int duration = minDistance(state, target);
        return state.time() + duration < timeWindows[target].end();
    }

    private int minDistance(TSPTWState from, Integer to) {
        return switch (from.position()) {
            case TSPNode(int value) -> timeMatrix[value][to];
            case Virtual(Set<Integer> nodes) ->
                    nodes.stream().mapToInt(x -> x).map(x -> timeMatrix[x][to]).max().getAsInt();
        };
    }

    private int arrivalTime(TSPTWState from, Integer to) {
        int time = from.time() + minDistance(from, to);
        return Math.max(time, timeWindows[to].start());

    }

}
