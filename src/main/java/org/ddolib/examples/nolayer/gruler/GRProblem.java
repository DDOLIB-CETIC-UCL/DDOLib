package org.ddolib.examples.nolayer.gruler;

import org.ddolib.modeling.InvalidSolutionException;
import org.ddolib.modeling.nolayer.Problem;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;

public class GRProblem implements Problem<GRState> {

    public final int order;

    public GRProblem(int order) {
        this.order = order;
    }

    @Override
    public GRState initialState() {
        BitSet mark = new BitSet();
        mark.set(0);
        return new GRState(mark, new BitSet(), 0, 1);
    }

    @Override
    public double initialValue() {
        return 0;
    }

    @Override
    public boolean isTarget(GRState state) {
        return state.getNumberOfMarks() == order;
    }

    @Override
    public Iterator<Integer> domain(GRState state) {
        List<Integer> dom = new ArrayList<>();
        int nextMark = state.getLastMark() + 1;
        // The maximum length of a Golomb ruler of order n is strictly less than n^2
        int maxL = order * order;
        
        for (int label = nextMark; label < maxL; label++) {
            boolean legal = true;
            for (int mark = state.getMarks().nextSetBit(0); mark >= 0; mark = state.getMarks().nextSetBit(mark + 1)) {
                if (state.getDistances().get(label - mark)) {
                    legal = false;
                    break;
                }
            }
            if (legal) {
                dom.add(label);
            }
        }
        return dom.iterator();
    }

    @Override
    public GRState transition(GRState state, int label) {
        BitSet nextMarks = (BitSet) state.getMarks().clone();
        BitSet nextDistances = (BitSet) state.getDistances().clone();

        for (int mark = state.getMarks().nextSetBit(0); mark >= 0; mark = state.getMarks().nextSetBit(mark + 1)) {
            nextDistances.set(label - mark);
        }
        nextMarks.set(label);

        return new GRState(nextMarks, nextDistances, label, state.getLayer() + 1);
    }

    @Override
    public double transitionCost(GRState state, int label) {
        return label - state.getLastMark();
    }

    @Override
    public double evaluate(int[] solution) throws InvalidSolutionException {
        int nbVars = order - 1;
        if (solution.length != nbVars) {
            throw new InvalidSolutionException(String.format("The solution %s does not match " +
                    "the number %d variables", Arrays.toString(solution), nbVars));
        }
        if (nbVars == 0) return 0;

        Map<Integer, Integer[]> distance = new HashMap<>();

        for (int j = 0; j < solution.length; j++) {
            distance.put(solution[j], new Integer[]{0, j + 1});
        }

        for (int i = 1; i < order; i++) {
            for (int j = i + 1; j < order; j++) {
                int from = solution[i - 1];
                int to = solution[j - 1];
                int d = to - from;
                if (distance.containsKey(d)) {
                    Integer[] pair = distance.get(d);
                    String msg = String.format("The marks %d & %d have the same distance (%d) " +
                            "than the marks %d & %d", i, j, d, pair[0], pair[1]);
                    throw new InvalidSolutionException(msg);
                }

                distance.put(d, new Integer[]{i, j});
            }
        }
        return solution[solution.length - 1];
    }

    @Override
    public String toString() {
        return "GRProblem(order:" + order + ")";
    }
}
