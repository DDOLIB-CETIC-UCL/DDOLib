package org.ddolib.examples.hrcp;

import org.ddolib.modeling.FastLowerBound;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * Admissible lower-bound heuristic for the HRCP scheduling problem,
 * adapted from the workload-balancing strategy of Jiaxin (SSALBRBFastLowerBound).
 * <p>
 * Combines four complementary bounds and returns the tightest:
 * <ol>
 *     <li><b>Conversion-rate workload balance</b> — optimally distributes remaining
 *         tasks between human and robot (continuous relaxation) and computes
 *         {@code max(tH + lh, tR + lr)}.  Tighter than the simple resource-contention
 *         bound because it explicitly optimises the assignment.</li>
 *     <li><b>Resource-contention bound</b> —
 *         {@code (tH + tR + W) / 2} where {@code W = Σ min(h, r, 2·c)}.</li>
 *     <li><b>Precedence bound</b> — {@code max(readiness[k] + minDur[k])} for
 *         remaining tasks.</li>
 *     <li><b>Critical-path bound</b> — forward-pass through remaining DAG combined
 *         with precomputed <em>tail</em> values (longest remaining chain of min
 *         durations to any sink).</li>
 * </ol>
 * All bounds are admissible (≤ the true remaining cost, i.e.&nbsp;the eventual makespan).
 *
 * @see HRCPProblem
 * @see HRCPState
 */
public class HRCPFastLowerBoundJiaxin implements FastLowerBound<HRCPState> {

    private final HRCPProblem problem;

    /**
     * {@code tail[k]} = minimum time from the start of task {@code k} to the end of
     * its longest successor chain (using {@code minDuration} for every task).
     */
    private final int[] tail;

    public HRCPFastLowerBoundJiaxin(HRCPProblem problem) {
        this.problem = problem;

        // Precompute tail values in reverse topological order.
        int n = problem.n;
        this.tail = new int[n];
        int[] topo = problem.topologicalOrder;
        for (int idx = topo.length - 1; idx >= 0; idx--) {
            int task = topo[idx];
            int bestSucc = 0;
            for (int succ : problem.successors[task]) {
                bestSucc = Math.max(bestSucc, tail[succ]);
            }
            tail[task] = problem.minDuration[task] + bestSucc;
        }
    }

    @Override
    public double fastLowerBound(HRCPState state, Set<Integer> variables) {
        // Terminal: all decisions (including dummy) have been made.
        if (variables.isEmpty()) {
            return 0;
        }

        int n = problem.n;
        long sched = state.scheduled;
        int tH = state.tH;
        int tR = state.tR;
        int currentMakespan = Math.max(tH, tR);

        int remainingCount = n - Long.bitCount(sched);
        if (remainingCount == 0) {
            // All tasks scheduled; only the dummy variable remains.
            // Its transition cost is max(tH, tR).
            return currentMakespan;
        }

        // ----------------------------------------------------------------
        // 1. Conversion-rate workload balance  (adapted from Section 3.3.1)
        // ----------------------------------------------------------------
        // We optimise:  min_assignment  max(tH + lh, tR + lr)
        // where lh / lr are the human / robot occupancy for remaining tasks.
        // Greedy conversion by ascending Cr with linear interpolation at the
        // crossing point gives the optimal continuous-relaxation bound.

        record ConversionCandidate(int task, int mode, double cr) {}

        double lh = 0.0;   // total human occupancy for remaining tasks
        double lr = 0.0;   // total robot occupancy for remaining tasks
        List<ConversionCandidate> candidates = new ArrayList<>(remainingCount);

        for (int k = 0; k < n; k++) {
            if ((sched & (1L << k)) != 0) continue;

            int hd = problem.humanDurations[k];
            int rd = problem.robotDurations[k];
            int cd = problem.collaborationDurations[k];

            boolean humanOk  = hd < HRCPProblem.BIG_M;
            boolean robotOk  = rd < HRCPProblem.BIG_M;
            boolean collabOk = cd < HRCPProblem.BIG_M;

            if (humanOk) {
                // Tentatively assign to human.
                lh += hd;

                // Compute conversion rate for each feasible non-human mode.
                double crR = robotOk && hd > 0
                        ? ((double) rd) / hd
                        : Double.POSITIVE_INFINITY;
                double crC = collabOk && hd > cd
                        ? ((double) cd) / (hd - cd)
                        : Double.POSITIVE_INFINITY;

                if (crR <= crC && robotOk) {
                    candidates.add(new ConversionCandidate(k, 1, crR));
                } else if (collabOk) {
                    candidates.add(new ConversionCandidate(k, 2, crC));
                }
                // If neither robot nor collab is feasible, task stays human.
            } else if (robotOk) {
                // Human infeasible → force robot.
                lr += rd;
            } else if (collabOk) {
                // Human & robot infeasible → force collab (ties up both resources).
                lh += cd;
                lr += cd;
            }
            // If all modes infeasible the task is simply ignored (shouldn't happen).
        }

        // Sort candidates by conversion rate (most efficient first).
        candidates.sort(Comparator.comparingDouble(ConversionCandidate::cr));

        // Iteratively convert until workloads balance: tR + lr >= tH + lh.
        boolean crossed = false;
        int crossingTask = -1;
        int crossingMode = -1;
        double crossingLhBefore = 0.0;
        double crossingLrBefore = 0.0;

        for (ConversionCandidate cand : candidates) {
            if (tR + lr >= tH + lh) break;   // balanced

            double lhBefore = lh;
            double lrBefore = lr;

            int hd = problem.humanDurations[cand.task()];
            int rd = problem.robotDurations[cand.task()];
            int cd = problem.collaborationDurations[cand.task()];

            if (cand.mode() == 1) {          // → robot
                lh -= hd;
                lr += rd;
            } else {                          // → collab
                lh -= (hd - cd);
                lr += cd;
            }

            if (!crossed && tR + lr > tH + lh) {
                crossed = true;
                crossingTask = cand.task();
                crossingMode = cand.mode();
                crossingLhBefore = lhBefore;
                crossingLrBefore = lrBefore;
                break;
            }
        }

        // Linear interpolation at the crossing task (fractional conversion).
        if (crossed) {
            int hd = problem.humanDurations[crossingTask];
            int rd = problem.robotDurations[crossingTask];
            int cd = problem.collaborationDurations[crossingTask];

            double diff = (tH + crossingLhBefore) - (tR + crossingLrBefore);

            if (crossingMode == 1) {         // robot
                double denom = (double) hd + (double) rd;
                if (denom > 0.0) {
                    double alpha = Math.max(0.0, Math.min(1.0, diff / denom));
                    lh = crossingLhBefore - alpha * hd;
                    lr = crossingLrBefore + alpha * rd;
                }
            } else {                          // collab
                double dh = hd - cd;
                double denom = dh + cd;       // = hd
                if (denom > 0.0) {
                    double alpha = Math.max(0.0, Math.min(1.0, diff / denom));
                    lh = crossingLhBefore - alpha * dh;
                    lr = crossingLrBefore + alpha * cd;
                }
            }
        }

        double lbConversion = Math.max(tH + lh, tR + lr);

        // ----------------------------------------------------------------
        // 2. Resource-contention bound   (tH + tR + W) / 2
        //    W = Σ min(h, r, 2·c)  (collab ties up BOTH resources)
        // ----------------------------------------------------------------
        double totalWork = 0.0;
        for (int k = 0; k < n; k++) {
            if ((sched & (1L << k)) != 0) continue;
            totalWork += Math.min(
                    Math.min(problem.humanDurations[k], problem.robotDurations[k]),
                    2 * problem.collaborationDurations[k]);
        }
        double lbWork = (tH + tR + totalWork) / 2.0;

        // ----------------------------------------------------------------
        // 3. Precedence bound   max( readiness[k] + minDur[k] )
        // ----------------------------------------------------------------
        double lbPrec = currentMakespan;
        for (int k = 0; k < n; k++) {
            if ((sched & (1L << k)) != 0) continue;
            lbPrec = Math.max(lbPrec,
                    (double) state.readiness[k] + problem.minDuration[k]);
        }

        // ----------------------------------------------------------------
        // 4. Critical-path bound  (forward-pass + precomputed tails)
        // ----------------------------------------------------------------
        //   Forward-pass gives precise es[k] accounting for readiness AND
        //   chains of unscheduled predecessors.  Combined with tail[k] this
        //   gives:  es[k] + tail[k]  (the full chain through k to any sink).
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

        double lbCp = currentMakespan;
        for (int k = 0; k < n; k++) {
            if ((sched & (1L << k)) != 0) continue;
            // ef[k] already incorporates the forward path TO k.
            // tail[k] - minDuration[k] gives the remaining chain AFTER k.
            // Combined: ef[k] + (tail[k] - minDuration[k]) = es[k] + tail[k].
            int es = ef[k] - problem.minDuration[k];
            lbCp = Math.max(lbCp, (double) es + tail[k]);
        }

        // ----------------------------------------------------------------
        // Return the tightest bound.
        // ----------------------------------------------------------------
        double lb = currentMakespan;
        lb = Math.max(lb, lbConversion);
        lb = Math.max(lb, lbWork);
        lb = Math.max(lb, lbPrec);
        lb = Math.max(lb, lbCp);
        return lb;
    }
}

