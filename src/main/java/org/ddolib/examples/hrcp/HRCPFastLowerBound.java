package org.ddolib.examples.hrcp;

import org.ddolib.modeling.FastLowerBound;

import java.util.Set;

/**
 * Admissible lower-bound heuristic for the HRCP scheduling problem.
 * <p>
 * Combines three complementary bounds and returns the tightest:
 * <ol>
 *     <li><b>{@code max(tH, tR)}</b> — both resources must finish their current work.</li>
 *     <li><b>Resource-contention bound</b> (precedence-free, inspired by the HRC package):
 *         {@code (tH + tR + W) / 2} where {@code W} is the sum over remaining tasks of
 *         {@code min(h[j], r[j], 2·c[j])}.  This captures the minimum total work that
 *         must be shared between two resources, regardless of precedences.</li>
 *     <li><b>Critical-path bound</b> — longest path through the remaining precedence
 *         DAG using the minimum per-task duration.  Ignores resource contention.</li>
 * </ol>
 * All three bounds are admissible (≤ the true remaining cost).
 */
public class HRCPFastLowerBound implements FastLowerBound<HRCPState> {

    private final HRCPProblem problem;

    public HRCPFastLowerBound(HRCPProblem problem) {
        this.problem = problem;
    }

    @Override
    public double fastLowerBound(HRCPState state, Set<Integer> variables) {
        int n = problem.n;
        long sched = state.scheduled;

        // --- Bound 1: resource availability ---
        int lb = Math.max(state.tH, state.tR);

        // --- Bound 2: resource-contention (HRC-style, ignores precedences) ---
        // makespan >= (tH + tR + W) / 2
        // where W = sum of min(h[j], r[j], 2*c[j]) for remaining tasks
        // (collab ties up BOTH resources, contributing 2*c[j] to total work)
        int totalWork = 0;
        for (int k = 0; k < n; k++) {
            if ((sched & (1L << k)) == 0) {
                totalWork += Math.min(
                        Math.min(problem.humanDurations[k], problem.robotDurations[k]),
                        2 * problem.collaborationDurations[k]);
            }
        }
        double contentionLB = (state.tH + state.tR + totalWork) / 2.0;

        // --- Bound 3: critical-path through remaining precedence DAG ---
        int[] ef = new int[n];
        for (int idx = 0; idx < n; idx++) {
            int k = problem.topologicalOrder[idx];
            if ((sched & (1L << k)) != 0) continue;

            int es = state.readiness[k];
            for (int p : problem.predecessors[k]) {
                if ((sched & (1L << p)) == 0) {
                    es = Math.max(es, ef[p]);
                }
            }
            ef[k] = es + problem.minDuration[k];
        }

        int criticalPathLB = 0;
        for (int k = 0; k < n; k++) {
            if ((sched & (1L << k)) == 0) {
                criticalPathLB = Math.max(criticalPathLB, ef[k]);
            }
        }

        return Math.max(lb, Math.max(contentionLB, criticalPathLB));
    }
}
