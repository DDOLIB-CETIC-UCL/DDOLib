package org.ddolib.examples.gruler;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.InvalidSolutionException;
import org.ddolib.modeling.Problem;

import java.util.*;
import java.util.stream.IntStream;

/**
 * Represents an instance of the Golomb Ruler (GR) problem.
 * <p>
 * The Golomb Ruler problem consists in placing {@code n} marks on a ruler such that
 * all pairwise distances between marks are distinct, and the length of the ruler
 * (i.e., the position of the last mark) is minimized.
 * </p>
 *
 * <p>
 * This class defines the problem structure and state transitions to be used
 * in optimization or search algorithms (e.g., A*, DDO, or constraint programming).
 * Each state represents a partially constructed ruler with a set of marks and
 * the distances between them.
 * </p>
 *
 * <p><b>Example:</b></p>
 * <pre>{@code
 * GRProblem problem = new GRProblem(4);
 * GRState initial = problem.initialState();
 * Iterator<Integer> domain = problem.domain(initial, 0);
 * }</pre>
 *
 * @see GRState
 * @see Decision
 * @see Problem
 */
public class GRProblem implements Problem<GRState> {
    /**
     * The desired number of marks on the ruler.
     */
    final int n;
    /**
     * The known optimal value for the instance, if available.
     */
    private Optional<Double> optimal = Optional.empty();

    /**
     * Constructs a Golomb Ruler problem with {@code n} marks and no known optimal value.
     *
     * @param n the number of marks to place on the ruler.
     */
    public GRProblem(int n) {
        this.n = n;
    }

    /**
     * Constructs a Golomb Ruler problem with {@code n} marks and a known optimal length.
     *
     * @param n       the number of marks to place on the ruler.
     * @param optimal the known optimal length of the ruler.
     */
    public GRProblem(int n, double optimal) {
        this.n = n;
        this.optimal = Optional.of(-optimal); //TODO remove -
    }

    /**
     * Returns the known optimal value of the problem, if available.
     *
     * @return an {@link Optional} containing the optimal value (negated),
     * or empty if the optimal value is unknown.
     */
    @Override
    public Optional<Double> optimalValue() {
        return optimal;
    }

    /**
     * Returns a human-readable representation of the problem instance.
     *
     * @return a string describing the Golomb Ruler instance.
     */
    @Override
    public String toString() {
        return String.format("GRuler: %d", n);
    }

    /**
     * Returns the number of variables in the problem.
     * For a ruler with {@code n} marks, there are {@code n - 1} decision variables.
     *
     * @return the number of decision variables.
     */
    @Override
    public int nbVars() {
        return n - 1;
    }

    /**
     * Returns the initial cost (always 0).
     *
     * @return the initial cost value.
     */
    @Override
    public double initialValue() {
        return 0;
    }

    /**
     * Returns the initial state of the problem, containing only the first mark at position 0.
     *
     * @return the initial {@link GRState}.
     */
    @Override
    public GRState initialState() {
        //Initialize with the first mark
        BitSet mark = new BitSet();
        mark.set(0);
        return new GRState(mark, new BitSet(), 0);
    }

    /**
     * Returns the possible domain values for the next decision (i.e., possible positions for the next mark).
     * <p>
     * The method ensures that no pairwise distance between existing marks and the new mark
     * duplicates an already existing distance.
     * </p>
     *
     * @param state the current state of the ruler.
     * @param var   the index of the variable being expanded.
     * @return an iterator over the feasible next mark positions.
     */
    @Override
    public Iterator<Integer> domain(GRState state, int var) {
        int nextMark = state.getLastMark() + 1;
        int n2 = n * n;
        return IntStream.range(nextMark, n2)
                .filter(i -> state.getMarks().stream().noneMatch(j -> state.getDistances().get(i - j)))
                .boxed()
                .iterator();
    }

    /**
     * Computes the next state resulting from applying a decision (adding a new mark).
     * <p>
     * The new state updates the set of marks and the set of pairwise distances.
     * </p>
     *
     * @param state    the current state.
     * @param decision the decision representing the position of the new mark.
     * @return a new {@link GRState} representing the updated configuration.
     */
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

    /**
     * Computes the cost associated with a transition between states.
     * <p>
     * The cost corresponds to the distance between the newly placed mark
     * and the previous one.
     * </p>
     *
     * @param state    the current state.
     * @param decision the decision leading to the next mark placement.
     * @return the incremental cost, equal to {@code decision.val() - state.getLastMark()}.
     */
    @Override
    public double transitionCost(GRState state, Decision decision) {
        return decision.val() - state.getLastMark();
    }

    @Override
    public double evaluate(int[] solution) throws InvalidSolutionException {
        if (solution.length != nbVars()) {
            throw new InvalidSolutionException(String.format("The solution %s does not match " +
                    "the number %d variables", Arrays.toString(solution), nbVars()));
        }
        if (nbVars() == 0) return 0;

        Map<Integer, Integer[]> distance = new HashMap<>();

        for (int j = 0; j < solution.length; j++) {
            distance.put(solution[j], new Integer[]{0, j + 1});
        }

        for (int i = 1; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
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
}
