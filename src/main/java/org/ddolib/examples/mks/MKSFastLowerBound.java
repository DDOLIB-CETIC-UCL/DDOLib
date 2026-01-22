package org.ddolib.examples.mks;

import org.ddolib.modeling.FastLowerBound;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;
/**
 * Provides a fast lower bound estimation for Multi-dimensional Knapsack (MKS) states.
 *
 * <p>
 * This class implements {@link FastLowerBound} and computes a simple, fast lower bound
 * on the negative total profit that can be achieved by a given set of variables (items).
 * The lower bound does not consider capacities or interactions between dimensions,
 * but only sums the profits of the candidate items.
 */
public class MKSFastLowerBound implements FastLowerBound<MKSState> {
    /** The MKS problem instance for which the lower bound is computed. */
    private final MKSProblem problem;
    /**
     * Constructs a fast lower bound evaluator for the given MKS problem.
     *
     * @param problem the multi-dimensional knapsack problem
     */
    public MKSFastLowerBound(MKSProblem problem) {
        this.problem = problem;
    }
    /**
     * Computes a fast lower bound for a given state and a set of variables (items).
     *
     * <p>
     * The bound is calculated as the negated sum of the profits of the variables,
     * ignoring capacity constraints.
     *
     * @param state the current MKS state
     * @param variables the set of variable indices (items) to consider
     * @return a lower bound on the cost (negative total profit) achievable
     */
    @Override
    public double fastLowerBound(MKSState state, Set<Integer> variables) {
        int maxProfit = 0;
        for (Integer variable : variables) {
            maxProfit += problem.profit[variable];
        }
        return -maxProfit;
    }
}
