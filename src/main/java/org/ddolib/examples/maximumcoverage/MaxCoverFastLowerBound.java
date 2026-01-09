package org.ddolib.examples.maximumcoverage;

import org.ddolib.modeling.FastLowerBound;

import java.util.BitSet;
import java.util.Set;
/**
 * Fast lower bound computation for the Maximum Coverage problem.
 *
 * <p>
 * This class implements {@link FastLowerBound} and provides a cheap and
 * optimistic lower bound on the objective value from a given state.
 * The bound is based on the maximum cardinality of any subset in the instance.
 *
 * <p>
 * The lower bound assumes that each remaining decision variable can cover
 * at most {@code maxCardSet} new items, which yields a fast but coarse estimate.
 */
public class MaxCoverFastLowerBound implements FastLowerBound<MaxCoverState> {
    /** The MaxCover problem instance. */
    private final MaxCoverProblem problem;
    /** Maximum cardinality among all subsets in the instance. */
    int maxCardSet = 0;
    /**
     * Constructs a fast lower bound evaluator for a given MaxCover problem.
     *
     * <p>
     * During construction, the maximum subset cardinality is precomputed
     * to allow constant-time bound evaluation.
     *
     * @param problem the MaxCover problem instance
     */
    public MaxCoverFastLowerBound(MaxCoverProblem problem) {
        this.problem = problem;
        for (BitSet subset : problem.subSets) {
            int card = subset.cardinality();
            if (card > maxCardSet) {
                maxCardSet = card;
            }
        }
    }
    /**
     * Computes a fast lower bound on the objective value from a given state.
     *
     * <p>
     * The bound is computed by assuming that each remaining variable
     * can contribute at most {@code maxCardSet} additional covered items.
     * The result is returned as a negative value to match the minimization
     * formulation of the problem.
     *
     * @param state the current state (not explicitly used in this bound)
     * @param variables the set of remaining decision variables
     * @return a fast, optimistic lower bound on the objective value
     */

    @Override
    public double fastLowerBound(MaxCoverState state, Set<Integer> variables) {
        // int coveredItems = state.coveredItems().cardinality();
        int lb = variables.size() * maxCardSet;
        return -lb;
    }
}
