package org.ddolib.examples.tsptw;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.Problem;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class TSPTWProblem implements Problem<TSPTWState> {

    public final int[][] distance;
    public final TimeWindow[] timeWindows;
    public final Optional<Double> optimal;

    private final Optional<String> name;


    /**
     * Creates instance from data files.<br>
     * <p>
     * The expected format is the following:
     * <ul>
     *     <li>
     *         The first line must contain the number of variable. A second  optional value can be
     *         given: the expected objective value for an optimal solution.
     *     </li>
     *     <li>
     *         The time matrix.
     *     </li>
     *     <li>
     *         A time window for each node.
     *     </li>
     * </ul>
     *
     * @param fname The path to the input file.
     * @throws IOException If something goes wrong while reading input file.
     */
    public TSPTWProblem(String fname) throws IOException {
        int numVar = 0;
        int[][] dist = new int[0][0];
        TimeWindow[] tw = new TimeWindow[0];
        Optional<Double> opti = Optional.empty();

        try (BufferedReader br = new BufferedReader(new FileReader(fname))) {
            int lineCount = 0;
            String line;
            while ((line = br.readLine()) != null) {
                //Skip comment
                if (line.startsWith("#") || line.isEmpty()) {
                    continue;
                }
                if (lineCount == 0) {
                    String[] tokens = line.split("\\s+");
                    numVar = Integer.parseInt(tokens[0]);
                    dist = new int[numVar][numVar];
                    tw = new TimeWindow[numVar];
                    if (tokens.length == 2) opti = Optional.of(Double.parseDouble(tokens[1]));
                } else if (1 <= lineCount && lineCount <= numVar) {
                    int i = lineCount - 1;
                    String[] distanceFromI = line.split("\\s+");
                    dist[i] = Arrays.stream(distanceFromI).mapToInt(Integer::parseInt).toArray();
                } else {
                    int i = lineCount - 1 - numVar;
                    String[] twStr = line.split("\\s+");
                    tw[i] = new TimeWindow(Integer.parseInt(twStr[0]), Integer.parseInt(twStr[1]));
                }
                lineCount++;
            }
        }

        this.distance = dist;
        this.timeWindows = tw;
        this.optimal = opti;
        this.name = Optional.of(fname);
    }

    @Override
    public Optional<Double> optimalValue() {
        return optimal;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append("\n");
        sb.append(Arrays.toString(timeWindows)).append("\n");
        String timeStr = Arrays.stream(distance)
                .map(row -> Arrays.stream(row)
                        .mapToObj(x -> String.format("%4s", x))
                        .collect(Collectors.joining(" ")))
                .collect(Collectors.joining("\n"));
        sb.append(timeStr);
        return name.orElse(sb.toString());
    }

    @Override
    public int nbVars() {
        return distance.length;
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
        int waiting = arrival < timeWindows[to].start() ? timeWindows[to].start() - arrival : 0;
        return travel + waiting;

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
            case TSPNode(int value) -> distance[value][to];
            case VirtualNodes(Set<Integer> nodes) ->
                    nodes.stream().mapToInt(x -> distance[x][to]).min().getAsInt();
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





