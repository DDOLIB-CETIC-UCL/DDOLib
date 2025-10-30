package org.ddolib.examples.tsptw;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.Problem;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Class representing an instance of the Traveling Salesman Problem with Time Windows (TSPTW).
 *
 * <p>
 * Each node has a time window during which it must be visited, and travel between nodes
 * is defined by a distance matrix. This class implements the {@link Problem} interface
 * for use in decision diagram solvers.
 * </p>
 *
 * <p>
 * The problem instance can optionally provide the known optimal solution.
 * </p>
 *
 * <h2>Data file format</h2>
 * <ul>
 *     <li>First line: number of nodes (variables). Optionally, a second value may be provided for the optimal objective.</li>
 *     <li>Next lines: the distance matrix, one row per node.</li>
 *     <li>Following lines: time windows for each node, specified as two integers (start and end) per line.</li>
 * </ul>
 *
 * <p>
 * The distance matrix defines the travel times between nodes, and the {@link TimeWindow} array defines
 * the allowed visit times for each node.
 * </p>
 */
public class TSPTWProblem implements Problem<TSPTWState> {

    /**
     * Distance matrix between nodes. distance[i][j] is the travel time from node i to node j.
     */
    public final int[][] distance;

    /**
     * Time windows for each node.
     */
    public final TimeWindow[] timeWindows;

    /**
     * Optional known optimal value for the instance.
     */
    public final Optional<Double> optimal;

    /**
     * Optional name of the instance, typically the file path.
     */
    private final Optional<String> name;


    /**
     * Constructs a TSPTW problem instance from a data file.
     *
     * @param fname Path to the input file containing the TSPTW instance.
     * @throws IOException If an error occurs while reading the file.
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

    @Override
    public double evaluate(int[] solution) throws InvalidSolutionException {
        if (solution.length != nbVars()) {
            throw new InvalidSolutionException(String.format("The solution %s does not cover all " +
                    "the %d variables", Arrays.toString(solution), nbVars()));
        }

        Map<Integer, Long> count = Arrays.stream(solution)
                .boxed()
                .collect(Collectors.groupingBy(x -> x, Collectors.counting()));

        if (count.values().stream().anyMatch(x -> x != 1)) {
            String msg = "The solution has duplicated nodes and does reached each node";
            throw new InvalidSolutionException(msg);
        }

        double value = distance[0][solution[0]]; //Start from the depot.
        value += Math.max(0, timeWindows[solution[0]].start() - value);


        for (int i = 1; i < nbVars(); i++) {
            int from = solution[i - 1];
            int to = solution[i];
            value += distance[from][to];
            if (value > timeWindows[to].end()) {
                String msg = String.format("This solution does not respect time windows. \nYou " +
                                "arrive at node %d at time %f. Its time window is %s", to, value,
                        timeWindows[to]);
                throw new InvalidSolutionException(msg);
            }
            value += Math.max(0, timeWindows[to].start() - value);
        }


        return value;
    }

    /**
     * Checks if a target node is reachable from the given state within its time window.
     *
     * @param from Current state.
     * @param to   Target node.
     * @return {@code true} if node can be reached before its time window closes; {@code false} otherwise.
     */
    boolean reachable(TSPTWState from, Integer to) {
        int duration = minDuration(from, to);
        return from.time() + duration <= timeWindows[to].end();
    }

    /**
     * Computes the minimal duration to reach a target node from the current state.
     *
     * @param from Current state.
     * @param to   Target node.
     * @return Minimum travel time to reach node {@code to}.
     */
    int minDuration(TSPTWState from, Integer to) {
        return switch (from.position()) {
            case TSPNode(int value) -> distance[value][to];
            case VirtualNodes(Set<Integer> nodes) ->
                    nodes.stream().mapToInt(x -> distance[x][to]).min().getAsInt();
        };
    }

    /**
     * Computes the arrival time at a target node, accounting for travel time and waiting
     * until the time window opens.
     *
     * @param from Current state.
     * @param to   Target node.
     * @return Arrival time at node {@code to}.
     */
    int arrivalTime(TSPTWState from, Integer to) {
        int time = from.time() + minDuration(from, to);
        return Integer.max(time, timeWindows[to].start());
    }

}





