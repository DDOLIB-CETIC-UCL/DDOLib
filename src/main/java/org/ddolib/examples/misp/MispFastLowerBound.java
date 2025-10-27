package org.ddolib.examples.misp;

import org.ddolib.modeling.FastLowerBound;

import java.util.BitSet;
import java.util.Set;

/**
 * Computes a fast lower bound for the Maximum Independent Set Problem (MISP).
 * <p>
 * This implementation of {@link FastLowerBound} estimates the best possible solution
 * from a given state by considering all remaining vertices optimistically.
 * The bound is used in search algorithms to prune suboptimal branches efficiently.
 * </p>
 */
public class MispFastLowerBound implements FastLowerBound<BitSet> {
    /**
     * The MISP problem instance for which the lower bound is computed.
     */
    private final MispProblem problem;

    /**
     * Constructs a fast lower bound calculator for a given MISP problem.
     *
     * @param problem the MISP problem instance
     */

    public MispFastLowerBound(MispProblem problem) {
        this.problem = problem;
    }

    /**
     * Computes a fast lower bound for the given state and set of remaining variables.
     * <p>
     * The method sums the weights of all vertices already selected in the current {@code state}
     * and returns its negation, which corresponds to the maximal independent set assumption.
     * </p>
     *
     * @param state     the current state of the solution as a {@link BitSet}
     * @param variables the set of remaining variable indices (unused in this implementation)
     * @return a fast lower bound on the optimal solution value
     */
    @Override
    public double fastLowerBound(BitSet state, Set<Integer> variables) {
        // We select all the remaining nodes
        return -state.stream().map(i -> problem.weight[i]).sum() + 100;
    }
}
