package org.ddolib.ddo.examples;

import org.ddolib.ddo.core.*;
import org.ddolib.ddo.heuristics.StateRanking;
import org.ddolib.ddo.heuristics.VariableHeuristic;
import org.ddolib.ddo.implem.frontier.SimpleFrontier;
import org.ddolib.ddo.implem.heuristics.DefaultVariableHeuristic;
import org.ddolib.ddo.implem.heuristics.FixedWidth;
import org.ddolib.ddo.implem.solver.ParallelSolver;

import java.io.IOException;
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
            return -(decision.val() - state.lastMark); // put a minus to turn objective into maximization (ddo requirement
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

    public static class GolombRelax implements Relaxation<GolombState> {
        @Override
        public GolombState mergeStates(final Iterator<GolombState> states) {
            // take the intersection of the marks and distances sets
            GolombState curr = states.next();
            BitSet intersectionMarks = (BitSet) curr.marks.clone();
            BitSet intersectionDistances = (BitSet) curr.distances.clone();
            int lastMark = curr.lastMark;
            while (states.hasNext()) {
                GolombState state = states.next();
                intersectionMarks.and(state.marks);
                intersectionDistances.and(state.distances);
                lastMark = Math.min(lastMark, state.lastMark);
            }
            return new GolombState(intersectionMarks, intersectionDistances, lastMark);
        }

        @Override
        public int relaxEdge(GolombState from, GolombState to, GolombState merged, Decision d, int cost) {
            return cost;
        }
    }

    static class GolombRanking implements StateRanking<GolombState> {
        @Override
        public int compare(GolombState s1, GolombState s2) {
            return Integer.compare(s1.lastMark, s2.lastMark); // sort by last mark
        }
    }

    public static void main(final String[] args) throws IOException {
        GolombProblem problem = new GolombProblem(7);
        final GolombRelax relax = new GolombRelax();
        final GolombRanking ranking = new GolombRanking();
        final FixedWidth<GolombState> width = new FixedWidth<>(250);
        final VariableHeuristic<GolombState> varh = new DefaultVariableHeuristic();
        final Frontier<GolombState> frontier = new SimpleFrontier<>(ranking);


        final Solver solver = new ParallelSolver<GolombState>(
                Runtime.getRuntime().availableProcessors(),
                problem,
                relax,
                varh,
                ranking,
                width,
                frontier);

        long start = System.currentTimeMillis();
        solver.maximize();
        double duration = (System.currentTimeMillis() - start) / 1000.0;

        int[] solution = solver.bestSolution()
                .map(decisions -> {
                    int[] values = new int[problem.nbVars()];
                    for (Decision d : decisions) {
                        values[d.var()] = d.val();
                    }
                    return values;
                })
                .get();

        System.out.println(String.format("Duration : %.3f", duration));
        System.out.println(String.format("Objective: %d", solver.bestValue().get()));
        System.out.println(String.format("Solution : %s", Arrays.toString(solution)));
    }
}
