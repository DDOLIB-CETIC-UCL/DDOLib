package org.ddolib.ddo.examples.max2sat;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Relaxation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import static java.lang.Integer.max;
import static java.lang.Integer.min;
import static java.lang.Integer.signum;
import static java.lang.Math.abs;


public class Max2SatRelax implements Relaxation<Max2SatState> {

    private final Max2SatProblem problem;
    private final int[] overApprox;

    public Max2SatRelax(Max2SatProblem problem) {
        this.problem = problem;
        overApprox = precomputeOverApproximation();
    }

    @Override
    public Max2SatState mergeStates(Iterator<Max2SatState> states) {
        ArrayList<Integer> merged = new ArrayList<>(Collections.nCopies(problem.nbVars(), 0));
        int depth = problem.nbVars();
        while (states.hasNext()) {
            Max2SatState current = states.next();
            depth = current.depth();
            for (int i = 0; i < current.netBenefit().size(); i++) {
                Integer mergedI = merged.get(i);
                Integer currentI = current.netBenefit().get(i);
                if (signum(mergedI) == 1 && signum(currentI) == 1) {
                    merged.set(i, min(mergedI, currentI));
                } else if (signum(mergedI) == -1 && signum(currentI) == 1) {
                    merged.set(i, max(mergedI, currentI));
                } else {
                    merged.set(i, 0);
                }
            }
        }
        return new Max2SatState(merged, depth);
    }

    @Override
    public int relaxEdge(Max2SatState from, Max2SatState to, Max2SatState merged, Decision d, int cost) {
        int toReturn = cost;
        for (int i = d.var() + 1; i < problem.nbVars(); i++) {
            toReturn += abs(to.netBenefit().get(i)) - abs(merged.netBenefit().get(i));
        }

        return toReturn;
    }

    /**
     * Some part of the upper bound depend only on the remaining variable, not the state. This part can be precomputed.
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

    @Override
    public int fastUpperBound(Max2SatState state, Set<Integer> variables) {
        return Max2SatRanking.rank(state) + overApprox[state.depth()];
    }
}
