package org.ddolib.examples.max2sat;

import org.ddolib.modeling.FastLowerBound;

import java.util.Set;

import static java.lang.Integer.max;

public class Max2SatFastLowerBound implements FastLowerBound<Max2SatState> {
    private final Max2SatProblem problem;
    private final int[] overApprox;

    private final int[] precomputationForUnary;

    public Max2SatFastLowerBound(Max2SatProblem problem) {
        this.problem = problem;
        overApprox = precomputeOverApproximation();
        precomputationForUnary = precomputeUnary();
    }


    @Override
    public double fastLowerBound(Max2SatState state, Set<Integer> variables) {
        int k = state.depth();
        if (k == problem.nbVars()) return 0.0;
        else
            return Max2SatRanking.rank(state) + precomputationForUnary[state.depth()] + overApprox[state.depth()];
    }

    private int[] precomputeUnary() {
        int[] toReturn = new int[problem.nbVars()];
        for (int i = problem.nbVars() - 1; i >= 0; i--) {
            int approx = max(problem.weight(problem.t(i), problem.t(i)), problem.weight(problem.f(i), problem.f(i)));
            if (i != problem.nbVars() - 1) {
                approx += toReturn[i + 1];
            }
            toReturn[i] = approx;
        }
        return toReturn;
    }

    /**
     * Some part of the upper bound depends only on the remaining variable, not the state. This part can be precomputed.
     * <br>
     * <p>
     * We compute here an over approximation of the optimal solution of the sub-problem composed of the remaining
     * variables. If the depth of the state is <code>k</code>, for each pairs of <code>k <= i < j < n</code> we compute:
     * <ul>
     *     <li><code>gtt(i, j)</code>, the sum of the weights of the clauses <code>(x<sub>i</sub> ||
     *     x<sub>j</sub>)</code>, <br> <code>(x<sub>i</sub> || !x<sub>j</sub>)</code> and <code>(!x<sub>i</sub> ||
     *     x<sub>j</sub>)</code></li>
     *
     *     <li><code>gtf(i, j)</code>, the sum of the weights of the clauses <code>(x<sub>i</sub> ||
     *       x<sub>j</sub>)</code>, <br> <code>(x<sub>i</sub> || !x<sub>j</sub>)</code> and <code>(!x<sub>i</sub> ||
     *     !x<sub>j</sub>)</code></li>
     *
     *     <li><code>gft(i, j)</code>, the sum of the weights of the clauses <code>(x<sub>i</sub> ||
     *     x<sub>j</sub>)</code>, <br> <code>(!x<sub>i</sub> || x<sub>j</sub>)</code> and <code>(!x<sub>i</sub> ||
     *     !x<sub>j</sub>)</code></li>
     *
     *     <li><code>gff(i, j)</code>, the sum of the weights of the clauses <code>(x<sub>i</sub> ||
     *      !x<sub>j</sub>)</code>, <br> <code>(!x<sub>i</sub> || x<sub>j</sub>)</code> and <code>(!x<sub>i</sub> ||
     *      !x<sub>j</sub>)</code></li>
     * </ul>
     * <p>
     * Finally, we sum <code>max{gtt(i, j), gtf(i, j), gft(i, j), gff(i,j)}</code>.
     */
    private int[] precomputeOverApproximation() {
        int[] toReturn = new int[problem.nbVars()];

        toReturn[problem.nbVars() - 1] = 0;
        for (int i = problem.nbVars() - 2; i >= 0; i--) {
            int approx = 0;
            for (int j = i + 1; j < problem.nbVars(); j++) {
                int gtt = problem.weight(problem.t(i), problem.t(j)) + problem.weight(problem.t(i), problem.f(j)) +
                        problem.weight(problem.f(i), problem.t(j));

                int gtf = problem.weight(problem.t(i), problem.t(j)) + problem.weight(problem.t(i), problem.f(j)) +
                        problem.weight(problem.f(i), problem.f(j));

                int gft = problem.weight(problem.t(i), problem.t(j)) + problem.weight(problem.f(i), problem.t(j)) +
                        problem.weight(problem.f(i), problem.f(j));

                int gff = problem.weight(problem.t(i), problem.f(j)) + problem.weight(problem.f(i), problem.t(j)) +
                        problem.weight(problem.f(i), problem.f(j));
                approx += max(max(gtt, gtf), max(gft, gff));
            }
            toReturn[i] = approx + toReturn[i + 1];
        }
        return toReturn;
    }


}
