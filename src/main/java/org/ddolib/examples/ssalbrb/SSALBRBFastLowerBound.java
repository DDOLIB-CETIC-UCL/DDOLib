package org.ddolib.examples.ssalbrb;

import org.ddolib.modeling.FastLowerBound;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SSALBRBFastLowerBound implements FastLowerBound<SSALBRBState> {

    private final SSALBRBProblem problem;
    private final int[] minDur;
    private final int[] tail;

    public SSALBRBFastLowerBound(SSALBRBProblem problem) {
        this.problem = problem;
        this.minDur = new int[problem.nbTasks];
        for (int i = 0; i < problem.nbTasks; i++) {
            int tH = problem.humanDurations[i];
            int tR = problem.robotDurations[i];
            int tC = problem.collaborationDurations[i];
            this.minDur[i] = Math.min(tH, Math.min(tR, tC));
        }

        this.tail = new int[problem.nbTasks];
        List<Integer> topo = topologicalOrder(problem.nbTasks, problem.successors);
        for (int idx = topo.size() - 1; idx >= 0; idx--) {
            int task = topo.get(idx);
            int bestSucc = 0;
            for (int succ : problem.successors.getOrDefault(task, List.of())) {
                bestSucc = Math.max(bestSucc, tail[succ]);
            }
            tail[task] = minDur[task] + bestSucc;
        }
    }

    private static List<Integer> topologicalOrder(int n, java.util.Map<Integer, java.util.List<Integer>> successors) {
        int[] indeg = new int[n];
        for (int i = 0; i < n; i++) {
            for (int succ : successors.getOrDefault(i, List.of())) {
                indeg[succ]++;
            }
        }

        ArrayList<Integer> order = new ArrayList<>(n);
        ArrayList<Integer> q = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            if (indeg[i] == 0) {
                q.add(i);
            }
        }

        for (int qi = 0; qi < q.size(); qi++) {
            int u = q.get(qi);
            order.add(u);
            for (int v : successors.getOrDefault(u, List.of())) {
                indeg[v]--;
                if (indeg[v] == 0) {
                    q.add(v);
                }
            }
        }

        return order;
    }

    @Override
    public double fastLowerBound(SSALBRBState state, Set<Integer> variables) {
        // Lower bound based on workload balancing strategy (conversion rate Cr_i) from Section 3.3.1.
        // To be consistent with the current DP model, we assume: m = 1 (one worker), q = 1 (one collaborative robot).

        // Step 1: Get the "remaining tasks set"
        Set<Integer> tasks;
        if (variables.isEmpty()) {
            // Extract unassigned tasks from state (E_t >= 0)
            tasks = new HashSet<>();
            for (int i = 0; i < state.earliestStartTimes().size(); i++) {
                if (state.isUnassigned(i)) {
                    tasks.add(i);
                }
            }
        } else {
            tasks = variables;
        }

        if (tasks.isEmpty()) {
            return 0.0;
        }

        final int m = 1;
        final int q = 1;

        // Candidates for "conversion operations" (robot or collaboration mode) to be sorted
        record ConversionCandidate(int task, int mode, double cr) {}

        double lh = 0.0;                // LH: current total workload for human
        double sumMinProcessingTime = 0.0;
        double maxMinProcessingTime = 0.0; // max{ min_p t_i^p }, maximum of minimum processing times across all tasks
        List<ConversionCandidate> candidates = new ArrayList<>(tasks.size());

        // Step 2: Initialize LH = Σ_i t_i^H, and compute Cr_i with optimal mode (robot or collaboration)
        for (int task : tasks) {
            int tH = problem.humanDurations[task];
            int tR = problem.robotDurations[task];
            int tC = problem.collaborationDurations[task];

            lh += tH;

            int best = Math.min(tH, Math.min(tR, tC));
            sumMinProcessingTime += best;
            if (best > maxMinProcessingTime) {
                maxMinProcessingTime = best;
            }

            // Cr_i^R = t_i^R / t_i^H (conversion rate for robot mode)
            double crR = tH > 0 ? ((double) tR) / ((double) tH) : Double.POSITIVE_INFINITY;

            // Cr_i^C = t_i^C / (t_i^H - t_i^C), only meaningful when t_i^H > t_i^C
            double crC;
            if (tH > tC) {
                crC = ((double) tC) / ((double) (tH - tC));
            } else {
                crC = Double.POSITIVE_INFINITY;
            }

            // Take Cr_i = min{Cr_i^R, Cr_i^C}, and record the corresponding mode
            if (crR <= crC) {
                candidates.add(new ConversionCandidate(task, SSALBRBProblem.MODE_ROBOT, crR));
            } else {
                candidates.add(new ConversionCandidate(task, SSALBRBProblem.MODE_COLLABORATION, crC));
            }
        }

        // Step 3: Sort by Cr_i ascending (tasks with better "transfer from human to robot" efficiency first)
        candidates.sort(Comparator.comparingDouble(ConversionCandidate::cr));

        // Step 4: Iteratively apply "conversion", update LH / LR, until LR * m >= LH * q
        double lr = 0.0;      // LR: current total workload for robot
        boolean crossed = false;
        int crossingTask = -1;
        int crossingMode = -1;
        double crossingLhBefore = 0.0;
        double crossingLrBefore = 0.0;

        for (ConversionCandidate cand : candidates) {
            if (lr * m >= lh * q) {
                break;
            }

            int task = cand.task();
            int mode = cand.mode();

            double lhBefore = lh;
            double lrBefore = lr;

            int tH = problem.humanDurations[task];
            int tR = problem.robotDurations[task];
            int tC = problem.collaborationDurations[task];

            // Update LH / LR using equations (17), (18)
            if (mode == SSALBRBProblem.MODE_ROBOT) {
                // p_i in p_R (robot mode)
                lh -= tH;
                lr += tR;
            } else {
                // p_i in p_C (collaboration mode)
                lh -= (tH - tC);
                lr += tC;
            }

            // Record the first task that makes LR * m > LH * q, for "partial conversion" correction using equation (19)
            if (!crossed && lr * m > lh * q) {
                crossed = true;
                crossingTask = task;
                crossingMode = mode;
                crossingLhBefore = lhBefore;
                crossingLrBefore = lrBefore;
                break;
            }
        }

        // Step 5: If LR * m > LH * q occurred, apply linear interpolation correction to LH using equation (19)
        if (crossed) {
            int tH = problem.humanDurations[crossingTask];
            int tR = problem.robotDurations[crossingTask];
            int tC = problem.collaborationDurations[crossingTask];

            if (crossingMode == SSALBRBProblem.MODE_ROBOT) {
                double denom = m * ((double) tR) + q * ((double) tH);
                if (denom > 0.0) {
                    double alpha = (q * crossingLhBefore - m * crossingLrBefore) / denom;
                    alpha = Math.max(0.0, Math.min(1.0, alpha));
                    lh = crossingLhBefore - alpha * ((double) tH);
                    lr = crossingLrBefore + alpha * ((double) tR);
                }
            } else {
                double dh = (double) (tH - tC);
                double dr = (double) tC;
                double denom = m * dr + q * dh;
                if (denom > 0.0) {
                    double alpha = (q * crossingLhBefore - m * crossingLrBefore) / denom;
                    alpha = Math.max(0.0, Math.min(1.0, alpha));
                    lh = crossingLhBefore - alpha * dh;
                    lr = crossingLrBefore + alpha * dr;
                }
            }
        }

        // Step 6: LC = max{ LH / m, max_i min_p t_i^p }
        double lc = Math.max(lh / m, maxMinProcessingTime);

        double currentMakespan = state.makespan();
        double lbPrec = 0.0;
        for (int task : tasks) {
            int est = state.earliestStartTimes().get(task);
            if (est < 0) {
                continue;
            }

            lbPrec = Math.max(lbPrec, Math.max(0.0, ((double) est) + minDur[task] - currentMakespan));
        }

        double cpAbs = 0.0;
        for (int task : tasks) {
            int est = state.earliestStartTimes().get(task);
            if (est < 0) {
                continue;
            }
            cpAbs = Math.max(cpAbs, ((double) est) + ((double) tail[task]));
        }
        double lbCp = Math.max(0.0, cpAbs - currentMakespan);

        double rh = (double) state.humanAvailable();
        double rr = (double) state.robotAvailable();
        double lbWork = Math.max(0.0, (rh + rr + sumMinProcessingTime) / 2.0 - currentMakespan);

        // Return the lower bound of remaining makespan (increment), not total makespan;
        // The DDO framework will automatically add the current accumulated cost (currentMakespan).
        return Math.max(Math.max(lbPrec, lbWork), lbCp);
    }
}
