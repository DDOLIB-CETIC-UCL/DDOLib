package org.ddolib.examples.max2sat;

import org.ddolib.modeling.FastLowerBound;

import java.util.Set;

import static java.lang.Integer.max;

/**
 * Implementation of a fast lower bound heuristic for the <b>Maximum 2-Satisfiability (MAX2SAT)</b> problem.
 * <p>
 * This class provides a quick computation of a lower bound on the best possible solution
 * from a given partial assignment (state) in a MAX2SAT instance. It is designed to be
 * used in branch-and-bound, A*, or DDO algorithms to prune suboptimal branches efficiently.
 * </p>
 *
 * <p>
 * The heuristic combines:
 * </p>
 * <ul>
 *     <li>A ranking of the current state using {@link Max2SatRanking}.</li>
 *     <li>Precomputed contributions from unary clauses (single variable) that only depend on the variable's depth.</li>
 *     <li>An over-approximation of the optimal solution for the remaining subproblem (pairwise interactions of remaining variables).</li>
 * </ul>
 * <p>
 * Precomputations are performed once during construction to speed up repeated lower bound queries.
 * </p>
 *
 * @see Max2SatProblem
 * @see Max2SatState
 * @see FastLowerBound
 */
public class Max2SatFastLowerBound implements FastLowerBound<Max2SatState> {

    /** The MAX2SAT problem instance associated with this lower bound. */
    private final Max2SatProblem problem;

    /** Precomputed over-approximation of the optimal solution for the remaining variables. */
    private final int[] overApprox;

    /** Precomputed contributions for unary clauses for each depth. */
    private final int[] precomputationForUnary;

    /**
     * Constructs the fast lower bound for a given MAX2SAT problem instance.
     *
     * @param problem the MAX2SAT problem instance
     */
    public Max2SatFastLowerBound(Max2SatProblem problem) {
        this.problem = problem;
        overApprox = precomputeOverApproximation();
        precomputationForUnary = precomputeUnary();
    }

    /**
     * Computes the fast lower bound for a given state and set of remaining variables.
     *
     * @param state the current MAX2SAT state (partial assignment)
     * @param variables the set of remaining variable indices (not yet assigned)
     * @return a fast lower bound on the best possible solution from this state
     */
    @Override
    public double fastLowerBound(Max2SatState state, Set<Integer> variables) {
        int k = state.depth();
        if (k == problem.nbVars()) return 0.0;
        return -(Max2SatRanking.rank(state) + precomputationForUnary[state.depth()] + overApprox[state.depth()]);
    }

    /**
     * Precomputes the contributions of unary clauses for each depth in the search tree.
     * <p>
     * For each variable, the maximum weight that can be achieved by assigning it to true or false
     * is accumulated with the contributions of remaining variables.
     * </p>
     *
     * @return an array of unary contributions for each depth
     */
    private int[] precomputeUnary() {
        int[] toReturn = new int[problem.nbVars()];
        for (int i = problem.nbVars() - 1; i >= 0; i--) {
            int approx = Math.max(problem.weight(problem.t(i), problem.t(i)),
                    problem.weight(problem.f(i), problem.f(i)));
            if (i != problem.nbVars() - 1) {
                approx += toReturn[i + 1];
            }
            toReturn[i] = approx;
        }
        return toReturn;
    }

    /**
     * Precomputes an over-approximation of the optimal solution for all remaining variables.
     * <p>
     * For each pair of remaining variables i &lt; j, it considers the four possible
     * pairwise assignments and sums the maximum achievable weight. The cumulative sums
     * of these maxima provide a fast upper estimate of the remaining potential.
     * </p>
     *
     * @return an array of over-approximated scores for each depth in the search tree
     */
    private int[] precomputeOverApproximation() {
        int[] toReturn = new int[problem.nbVars()];
        toReturn[problem.nbVars() - 1] = 0;
        for (int i = problem.nbVars() - 2; i >= 0; i--) {
            int approx = 0;
            for (int j = i + 1; j < problem.nbVars(); j++) {
                int gtt = problem.weight(problem.t(i), problem.t(j))
                        + problem.weight(problem.t(i), problem.f(j))
                        + problem.weight(problem.f(i), problem.t(j));

                int gtf = problem.weight(problem.t(i), problem.t(j))
                        + problem.weight(problem.t(i), problem.f(j))
                        + problem.weight(problem.f(i), problem.f(j));

                int gft = problem.weight(problem.t(i), problem.t(j))
                        + problem.weight(problem.f(i), problem.t(j))
                        + problem.weight(problem.f(i), problem.f(j));

                int gff = problem.weight(problem.t(i), problem.f(j))
                        + problem.weight(problem.f(i), problem.t(j))
                        + problem.weight(problem.f(i), problem.f(j));

                approx += Math.max(Math.max(gtt, gtf), Math.max(gft, gff));
            }
            toReturn[i] = approx + toReturn[i + 1];
        }
        return toReturn;
    }
}
