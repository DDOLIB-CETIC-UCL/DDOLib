package org.ddolib.examples.ddo.tsptw;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.Problem;

import java.util.*;

public class TSPTWProblem implements Problem<TSPTWState> {

    TSPTWInstance instance;

    private Optional<String> name = Optional.empty();

    public TSPTWProblem(TSPTWInstance instance) {
        this.instance = instance;
    }

    public void setName(String name) {
        this.name = Optional.of(name);
    }

    @Override
    public Optional<Double> optimalValue() {
        return instance.optimal;
    }

    @Override
    public String toString() {
        return name.orElse(instance.toString());
    }

    @Override
    public int nbVars() {
        return instance.distance.length;
    }

    @Override
    public TSPTWState initialState() {
        BitSet must = new BitSet(nbVars());
        must.set(1, nbVars(), true);
        BitSet might = new BitSet(nbVars());
        return new TSPTWState(new TSPNode(0), 0, must, might, 0);
    }

    @Override
    public double initialValue() {
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
                if (!reachable(state, i)) {
                    // We found a node that is no more reachable at the current time.
                    // Eventually, this state will lead to unfeasible solution.
                    // So we return now an empty set of decision to cut the sub diagram.
                    return Collections.emptyIterator();
                } else {
                    toReturn.set(i, true);
                }
            }

            if (state.mustVisit().length() < nbVars() - state.depth()) {
                // The state is a merged state. Its mustVisit set can be too small. In that case, we can take decision
                // from the possiblyVisit state.
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
    public double transitionCost(TSPTWState state, Decision decision) {
        int to = decision.val();

        int travel = minDuration(state, to);
        int arrival = state.time() + travel;
        int waiting = arrival < instance.timeWindows[to].start() ? instance.timeWindows[to].start() - arrival : 0;
        return -(travel + waiting);

    }

    /**
     * @param from The current state of the mdd.
     * @param to   The target node.
     * @return Whether we can reach the node {@code to} before the end of its time window starting from state {@code
     * from}.
     */
    boolean reachable(TSPTWState from, Integer to) {
        int duration = minDuration(from, to);
        return from.time() + duration <= instance.timeWindows[to].end();
    }

    /**
     * @param from The current state of the mdd.
     * @param to   The target node.
     * @return The minimal duration to reach the node {@code to} starting from state {@code from}.
     */
    int minDuration(TSPTWState from, Integer to) {
        return switch (from.position()) {
            case TSPNode(int value) -> instance.distance[value][to];
            case VirtualNodes(Set<Integer> nodes) ->
                    nodes.stream().mapToInt(x -> instance.distance[x][to]).min().getAsInt();
        };
    }

    /**
     * Computes the arrival time starting at {@code from.time()} given the travel time and the start of the time
     * window of
     *
     * @param from The current state of the mdd.
     * @param to   The target node.
     * @return The arrival time at {@code to} starting at {@code from.time()}.
     */
    int arrivalTime(TSPTWState from, Integer to) {
        int time = from.time() + minDuration(from, to);
        return Integer.max(time, instance.timeWindows[to].start());
    }

}

/**
 * Interface to model the position of the vehicle in a {@link TSPTWState}.
 */
sealed interface Position permits TSPNode, VirtualNodes {
}


/**
 * Unique position of the vehicle.
 *
 * @param value Last position of the vehicle in the current route.
 */
record TSPNode(int value) implements Position {
    @Override
    public String toString() {
        return "" + value;
    }
}


/**
 * Used for merged states. The vehicle can be at all the position of the merged states.
 *
 * @param nodes All the position of the merged states.
 */
record VirtualNodes(Set<Integer> nodes) implements Position {
    @Override
    public String toString() {
        return nodes.toString();
    }
}


