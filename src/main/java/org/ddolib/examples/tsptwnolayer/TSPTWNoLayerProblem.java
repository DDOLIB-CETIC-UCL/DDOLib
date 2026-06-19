package org.ddolib.examples.tsptwnolayer;

import org.ddolib.modeling.InvalidSolutionException;
import org.ddolib.modeling.nolayer.NoLayerProblem;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class TSPTWNoLayerProblem implements NoLayerProblem<TSPTWNoLayerState> {

    public final int[][] distance;
    public final TimeWindow[] timeWindows;
    public final int nbVars;

    public TSPTWNoLayerProblem(int[][] distance, TimeWindow[] timeWindows) {
        this.distance = distance;
        this.timeWindows = timeWindows;
        this.nbVars = distance.length;
    }

    @Override
    public TSPTWNoLayerState initialState() {
        BitSet mustVisit = new BitSet(nbVars);
        mustVisit.set(1, nbVars, true);
        return new TSPTWNoLayerState(0, 0, mustVisit);
    }

    @Override
    public double initialValue() {
        return 0;
    }

    @Override
    public boolean isTarget(TSPTWNoLayerState state) {
        return state.mustVisit().isEmpty() && state.currentCity() == 0;
    }

    @Override
    public Iterator<Integer> domain(TSPTWNoLayerState state) {
        List<Integer> dom = new ArrayList<>();
        if (state.mustVisit().isEmpty()) {
            if (state.currentCity() != 0) {
                if (reachable(state, 0)) {
                    dom.add(0);
                }
            }
            return dom.iterator();
        }

        for (int i = state.mustVisit().nextSetBit(0); i >= 0; i = state.mustVisit().nextSetBit(i + 1)) {
            if (reachable(state, i)) {
                dom.add(i);
            } else {
                // If any mustVisit node is unreachable, this state is a dead end
                return Collections.emptyIterator();
            }
        }
        return dom.iterator();
    }

    @Override
    public TSPTWNoLayerState transition(TSPTWNoLayerState state, int label) {
        int newTime = arrivalTime(state, label);
        BitSet newMust = (BitSet) state.mustVisit().clone();
        newMust.set(label, false);
        return new TSPTWNoLayerState(label, newTime, newMust);
    }

    @Override
    public double transitionCost(TSPTWNoLayerState state, int label) {
        int travel = distance[state.currentCity()][label];
        int arrival = state.time() + travel;
        int waiting = Math.max(0, timeWindows[label].start() - arrival);
        return travel + waiting;
    }

    @Override
    public double evaluate(int[] solution) throws InvalidSolutionException {
        if (solution.length != nbVars) {
            throw new InvalidSolutionException(String.format("The solution %s does not cover all " +
                    "the %d variables", Arrays.toString(solution), nbVars));
        }

        java.util.Map<Integer, Long> count = Arrays.stream(solution)
                .boxed()
                .collect(java.util.stream.Collectors.groupingBy(x -> x, java.util.stream.Collectors.counting()));

        if (count.values().stream().anyMatch(x -> x != 1)) {
            String msg = "The solution has duplicated nodes and does not reach each node";
            throw new InvalidSolutionException(msg);
        }

        double value = distance[0][solution[0]]; //Start from the depot.
        value += Math.max(0, timeWindows[solution[0]].start() - value);


        for (int i = 1; i < nbVars; i++) {
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

    private boolean reachable(TSPTWNoLayerState from, int to) {
        int duration = distance[from.currentCity()][to];
        return from.time() + duration <= timeWindows[to].end();
    }

    private int arrivalTime(TSPTWNoLayerState from, int to) {
        int time = from.time() + distance[from.currentCity()][to];
        return Math.max(time, timeWindows[to].start());
    }

    @Override
    public String toString() {
        return "TSPTWNoLayerProblem(nbVars:" + nbVars + ")";
    }

    public static TSPTWNoLayerProblem fromFile(String fname) throws IOException {
        int numVar = 0;
        int[][] dist = new int[0][0];
        TimeWindow[] tw = new TimeWindow[0];

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
        return new TSPTWNoLayerProblem(dist, tw);
    }
}
