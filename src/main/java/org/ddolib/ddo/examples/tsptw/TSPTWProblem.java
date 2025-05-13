package org.ddolib.ddo.examples.tsptw;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Problem;

import java.util.*;
import java.util.stream.Collectors;

public class TSPTWProblem implements Problem<TSPTWState> {

    final int[][] durationMatrix;
    final TimeWindow[] timeWindows;
    public final Optional<Integer> optimal;
    private Optional<String> name = Optional.empty();

    public TSPTWProblem(int[][] durationMatrix, TimeWindow[] timeWindows, Optional<Integer> optimal) {
        this.durationMatrix = durationMatrix;
        this.timeWindows = timeWindows;
        this.optimal = optimal;
    }

    public TSPTWProblem(int[][] durationMatrix, TimeWindow[] timeWindows) {
        this.durationMatrix = durationMatrix;
        this.timeWindows = timeWindows;
        this.optimal = Optional.empty();
    }


    public void setName(String name) {
        this.name = Optional.of(name);
    }

    @Override
    public String toString() {
        if (name.isPresent()) {
            return name.get();
        } else {
            String twStr = Arrays.toString(timeWindows);
            String timeStr = Arrays.stream(durationMatrix)
                    .map(row -> Arrays.stream(row)
                            .mapToObj(x -> String.format("%4s", x))
                            .collect(Collectors.joining(" ")))
                    .collect(Collectors.joining("\n"));
            return twStr + "\n" + timeStr;
        }
    }

    @Override
    public int nbVars() {
        return durationMatrix.length;
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
    public int transitionCost(TSPTWState state, Decision decision) {
        int to = decision.val();

        int travel = minDuration(state, to);
        int arrival = state.time() + travel;
        int waiting = arrival < timeWindows[to].start() ? timeWindows[to].start() - arrival : 0;
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
        return from.time() + duration <= timeWindows[to].end();
    }

    /**
     * @param from The current state of the mdd.
     * @param to   The target node.
     * @return The minimal duration to reach the node {@code to} starting from state {@code from}.
     */
    int minDuration(TSPTWState from, Integer to) {
        return switch (from.position()) {
            case TSPNode(int value) -> durationMatrix[value][to];
            case VirtualNodes(Set<Integer> nodes) ->
                    nodes.stream().mapToInt(x -> durationMatrix[x][to]).min().getAsInt();
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
        return Integer.max(time, timeWindows[to].start());

    }

}
