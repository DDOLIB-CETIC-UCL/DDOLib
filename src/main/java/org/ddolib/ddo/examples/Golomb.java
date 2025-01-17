package org.ddolib.ddo.examples;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Problem;

import java.util.*;
import java.util.stream.IntStream;


/**
 * This class demonstrates how to implement a solver for the Golomb ruler problem.
 * For more information on this problem, see
 * <a href="https://en.wikipedia.org/wiki/Golomb_ruler">Golomb Ruler - Wikipedia</a>.
 *
 * This model was introduced by Willem-Jan van Hoeve.
 * In this model:
 * - Each variable/layer represents the position of the next mark to be placed.
 * - The domain of each variable is the set of all possible positions for the next mark.
 * - A mark can only be added if the distance between the new mark and all previous marks
 *   is not already present in the set of distances between marks.
 *
 * The cost of a transition is defined as the distance between the new mark and the
 * previous last mark. Consequently, the cost of a solution is the position of the last mark.
 */
public class Golomb {

    static class GolombState {
        private BitSet marks;         // Set of marks already placed
        private BitSet distances;     // Set of pairwise distances already present
        private int lastMark;         // Location of last mark

        public GolombState() {
            this.marks = new BitSet();
            this.distances = new BitSet();
            this.lastMark = -1;
        }

        public GolombState(BitSet marks, BitSet distances, int lastMark) {
            this.marks = (BitSet) marks.clone();
            this.distances = (BitSet) distances.clone();
            this.lastMark = lastMark;
        }

        public BitSet getMarks() {
            return marks;
        }

        public BitSet getDistances() {
            return distances;
        }

        public int getNumberOfMarks() {
            return marks.size();
        }

        public int getLastMark() {
            return lastMark;
        }

        public void addMark(int mark) {
            assert(mark > lastMark);
            lastMark = mark;
            marks.set(mark);
        }

        public void addDistance(int distance) {
            assert !distances.get(distance);
            distances.set(distance);
        }

        public GolombState copy() {
            return new GolombState(marks, distances, lastMark);
        }

        @Override
        public int hashCode() {
            return Objects.hash(marks, distances, lastMark);
        }
    }

    static class GolombProblem implements Problem<GolombState> {
        private final int n;

        public GolombProblem(int n) {
            this.n = n;
        }

        @Override
        public int nbVars() {
            return n;
        }

        @Override
        public int initialValue() {
            return 0;
        }

        @Override
        public GolombState initialState() {
            GolombState state = new GolombState();
            state.addMark(0); // Initialize with the first mark
            return state;
        }

        @Override
        public Iterator<Integer> domain(GolombState state, int var) {
            ArrayList<Integer> domain = new ArrayList<>();
            int nextMark = state.lastMark + 1;
            int n2 = n * n;
            domain.addAll(
                    IntStream.range(nextMark, n2)
                            .filter(i -> state.marks.stream().noneMatch(j -> state.distances.get(i - j)))
                            .boxed()
                            .toList());
            return  domain.iterator();
        }

        @Override
        public GolombState transition(GolombState state, Decision decision) {
            GolombState ret = state.copy();
            int newMark = decision.val();
            ret.addMark(newMark);
            // add distances between new mark and previous marks
            for (int i = state.marks.nextSetBit(0);
                 i >= 0;
                 i = state.marks.nextSetBit(i + 1)) {
                ret.addDistance(newMark - i);
            }
            return ret;
        }

        @Override
        public int transitionCost(GolombState state, Decision decision) {
            return 0;
        }

        public boolean isValidMark(GolombState state, int mark) {
            BitSet distances = state.getDistances();
            for (int i = state.getMarks().nextSetBit(0); i >= 0; i = state.getMarks().nextSetBit(i + 1)) {
                int distance = Math.abs(mark - i);
                if (distances.get(distance)) {
                    return false; // Distance already exists
                }
            }
            return true;
        }

        public GolombState transition(GolombState state, int newMark) {
            GolombState newState = state.copy();
            newState.addMark(newMark);
            for (int i = state.getMarks().nextSetBit(0); i >= 0; i = state.getMarks().nextSetBit(i + 1)) {
                int distance = Math.abs(newMark - i);
                newState.addDistance(distance);
            }
            newState.lastMark = newMark;
            return newState;
        }

        public int transitionCost(GolombState state, int newMark) {
            return -(newMark - state.getLastMark()); // Negative for maximization
        }
    }
}
