package org.ddolib.ddo.examples.gruler;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Problem;

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
        return n-1;
    }

    @Override
    public int initialValue() {
        return 0;
    }

    @Override
    public GRState initialState() {
        GRState state = new GRState();
        state.addMark(0); // Initialize with the first mark
        return state;
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
//        System.out.println(Arrays.toString(domain.toArray()));
        return  domain.iterator();
    }

    @Override
    public GRState transition(GRState state, Decision decision) {
        GRState ret = state.copy();
        int newMark = decision.val();
        ret.addMark(newMark);
        // add distances between new mark and previous marks
        for (int i = state.getMarks().nextSetBit(0);
             i >= 0;
             i = state.getMarks().nextSetBit(i + 1)) {
            ret.addDistance(newMark - i);
        }
        return ret;
    }

    @Override
    public int transitionCost(GRState state, Decision decision) {
        return -(decision.val() - state.getLastMark()); // put a minus to turn objective into maximization (ddo requirement
    }

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
        newState.addMark(newMark);
        for (int i = state.getMarks().nextSetBit(0); i >= 0; i = state.getMarks().nextSetBit(i + 1)) {
            int distance = Math.abs(newMark - i);
            newState.addDistance(distance);
        }
        newState.setLastMark(newMark);
        return newState;
    }

    public int transitionCost(GRState state, int newMark) {
        return -(newMark - state.getLastMark()); // Negative for maximization
    }
}
