package org.ddolib.examples.lcs;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.Relaxation;

import java.util.Arrays;
import java.util.Iterator;
/**
 * Relaxation strategy for {@link LCSState} in the Longest Common Subsequence (LCS) problem.
 * <p>
 * This class implements {@link Relaxation} and is used to merge multiple LCS states into
 * a single relaxed state, which is useful for search algorithms such as DDO (Decision Diagram Optimization).
 * </p>
 * <p>
 * The merged state preserves, for each string, the earliest position among all merged states.
 * This allows the algorithm to over-approximate the search space while maintaining feasibility.
 * </p>
 */
public class LCSRelax implements Relaxation<LCSState> {
    /** The LCS problem instance associated with this relaxation. */
    LCSProblem problem;
    /**
     * Constructs a relaxation object for a given LCS problem.
     *
     * @param problem The LCS problem instance.
     */
    public LCSRelax(LCSProblem problem) {
        this.problem = problem;
    }
    /**
     * Merges multiple LCS states into a single relaxed state.
     * <p>
     * For each string, the merged state takes the minimum position among all states,
     * effectively keeping the "earliest progress" along each string.
     * </p>
     *
     * @param states Iterator over the states to be merged.
     * @return A new {@link LCSState} representing the merged state.
     */
    @Override
    public LCSState mergeStates(Iterator<LCSState> states) {
        int[] position = Arrays.copyOf(problem.stringsLength, problem.stringsLength.length);

        // Merged LCSState keeps the earliest position of each String.
        while (states.hasNext()) {
            LCSState state = states.next();
            for (int i = 0; i < problem.stringNb; i++) {
                position[i] = Math.min(position[i], state.position[i]);
            }
        }

        return new LCSState(position);
    }
    /**
     * Relaxes the cost of a transition between two LCS states.
     * <p>
     * This implementation returns the original cost unchanged.
     * </p>
     *
     * @param from The state from which the transition originates.
     * @param to The state to which the transition goes.
     * @param merged The merged state that includes both 'from' and 'to'.
     * @param d The decision taken.
     * @param cost The original cost of the transition.
     * @return The relaxed cost, which in this case is the same as {@code cost}.
     */
    @Override
    public double relaxEdge(LCSState from, LCSState to, LCSState merged, Decision d, double cost) {
        return cost;
    }
}
