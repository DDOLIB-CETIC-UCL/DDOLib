package org.ddolib.examples.ddo.gruler;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.Problem;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.stream.IntStream;

public class GRProblem implements Problem<GRState> {
    final int n;

    public GRProblem(int n) {
        this.n = n;
    }

    @Override
    public int nbVars() {
        return n - 1;
    }

    @Override
    public double initialValue() {
        return 0;
    }

    @Override
    public GRState initialState() {
        //Initialize with the first mark
        BitSet mark = new BitSet();
        mark.set(0);
        return new GRState(mark, new BitSet(), 0);
    }

    @Override
    public Iterator<Integer> domain(GRState state, int var) {
        ArrayList<Integer> domain = new ArrayList<>();
        int nextMark = state.getLastMark() + 1;
        int n2 = n * n;
        domain.addAll(
                IntStream.range(nextMark, n2)
                        .filter(i -> state.getMarks().stream().noneMatch(j -> state.getDistances().get(i - j)))
                        .boxed()
                        .toList());
//        System.out.println(state + " --> " +  Arrays.toString(domain.toArray()));
        return domain.iterator();
    }

    @Override
    public GRState transition(GRState state, Decision decision) {
        GRState newState = state.copy();
        int newMark = decision.val();
        // add distances between new mark and previous marks
        BitSet newDistances = new BitSet();
        for (int i = state.getMarks().nextSetBit(0);
             i >= 0;
             i = state.getMarks().nextSetBit(i + 1)) {
            assert !newDistances.get(newMark - i);
            newDistances.set(newMark - i);
        }
        assert (newMark >= newState.getLastMark());
        newState.getMarks().set(newMark);
        newState.getDistances().or(newDistances);
        return new GRState(newState.getMarks(), newState.getDistances(), newMark);
    }

    @Override
    public double transitionCost(GRState state, Decision decision) {
        return -(decision.val() - state.getLastMark()); // put a minus to turn objective into maximization (ddosolver requirement
    }

    /*
    public boolean isValidMark(GRState state, int mark) {
        BitSet distances = state.getDistances();
        for (int i = state.getMarks().nextSetBit(0); i >= 0; i = state.getMarks().nextSetBit(i + 1)) {
            int distance = Math.abs(mark - i);
            if (distances.get(distance)) {
                return false; // Distance already exists
            }
        }
        return true;
    }

    public GRState transition(GRState state, int newMark) {
        GRState newState = state.copy();
        BitSet newDistances = new BitSet();
        for (int i = state.getMarks().nextSetBit(0); i >= 0; i = state.getMarks().nextSetBit(i + 1)) {
            int distance = Math.abs(newMark - i);
            assert !newDistances.get(distance);
            newDistances.set(distance);
        }
        assert newMark >= newState.getLastMark();
        newState.getMarks().set(newMark);
        newState.getDistances().or(newDistances);
        return new GRState(newState.getMarks(), newState.getDistances(), newMark);
    }

    public int transitionCost(GRState state, int newMark) {
        return -(newMark - state.getLastMark()); // Negative for maximization
    }
    */
}
