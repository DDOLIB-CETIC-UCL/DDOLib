package org.ddolib.examples.ssalbrb;

import org.ddolib.modeling.FastLowerBound;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SSALBRBFastLowerBound implements FastLowerBound<SSALBRBState> {

    private final SSALBRBProblem problem;
    private final int cycleTime;

    public SSALBRBFastLowerBound(SSALBRBProblem problem) {
        this(problem, Integer.MAX_VALUE);
    }

    public SSALBRBFastLowerBound(SSALBRBProblem problem, int cycleTime) {
        this.problem = problem;
        this.cycleTime = cycleTime;
    }

    @Override
    public double fastLowerBound(SSALBRBState state, Set<Integer> variables) {
        Set<Integer> tasks = new HashSet<>();
        for (int i = 0; i < state.earliestStartTimes().size(); i++) {
            if (state.isUnassigned(i)) {
                tasks.add(i);
            }
        }

        if (tasks.isEmpty()) {
            return 0.0;
        }

        return computeWorkloadBalancingLB(state, tasks);
    }

    /**
     * Workload balancing lower bound for the inner single-station scheduling problem.
     *
     * Mirrors the outer DDO LB1 algorithm (q=1, robot capacity = cycleTime),
     * extended to account for the current resource availability times rh and rr.
     *
     * Initializes LH = sum of human durations for all unscheduled tasks, LR = 0.
     * Sorts tasks by conversion rate theta_t = min(t_r/t_h, t_c/(t_h-t_c)) ascending.
     * Iteratively converts tasks from human mode to robot or collaboration mode, stopping when:
     *   (ii)  rr + LR > cycleTime (robot total load in this station exhausted), or
     *   (iii) rr + LR >= rh + LH (both resources finish at the same time — balanced).
     * Partial conversion is applied when either condition would be overshot.
     *
     * Final bound: max(0, max(rh + LH_final, rr + LR_final) - currentMakespan)
     * Derivation: human finishes no earlier than rh + LH_final,
     *             robot finishes no earlier than rr + LR_final.
     */
    private double computeWorkloadBalancingLB(SSALBRBState state, Set<Integer> tasks) {
        double currentMakespan = state.makespan();
        double rh = state.humanAvailable();
        double rr = state.robotAvailable();

        record TaskConv(double theta_ir, double theta_ic, int tH, int tR, int tC) {}

        double LH = 0.0;
        List<TaskConv> conversions = new ArrayList<>(tasks.size());

        for (int task : tasks) {
            int tH = problem.humanDurations[task];
            int tR = problem.robotDurations[task];
            int tC = problem.collaborationDurations[task];

            LH += tH;

            double theta_ir = (tR < 100000 && tH > 0)
                    ? (double) tR / tH : Double.POSITIVE_INFINITY;
            double theta_ic = (tC < 100000 && tH > tC)
                    ? (double) tC / (tH - tC) : Double.POSITIVE_INFINITY;

            conversions.add(new TaskConv(theta_ir, theta_ic, tH, tR, tC));
        }

        conversions.sort(Comparator.comparingDouble(tc -> Math.min(tc.theta_ir(), tc.theta_ic())));

        double LR = 0.0;

        for (TaskConv tc : conversions) {
            // Condition (iii): robot side already catches up with human side
            if (rr + LR >= rh + LH) break;

            double LH_before = LH;
            double LR_before = LR;

            if (tc.theta_ir() <= tc.theta_ic()) {
                // Robot mode
                if (tc.tR() < 100000) {
                    LH -= tc.tH();
                    LR += tc.tR();

                    // Condition (ii): robot capacity exceeded (rr + LR <= cycleTime)
                    if (rr + LR > cycleTime) {
                        if (tc.tR() > 0) {
                            double alpha = (cycleTime - rr - LR_before) / tc.tR();
                            alpha = Math.max(0.0, Math.min(1.0, alpha));
                            LH = LH_before - alpha * tc.tH();
                            LR = LR_before + alpha * tc.tR();
                        }
                        break;
                    }

                    // Condition (iii): overshot balance point (rr+LR = rh+LH)
                    // rr + LR_before + alpha*tR = rh + LH_before - alpha*tH
                    // alpha*(tR + tH) = (rh + LH_before) - (rr + LR_before)
                    if (rr + LR > rh + LH) {
                        double gap = (rh + LH_before) - (rr + LR_before);
                        double denom = tc.tH() + tc.tR();
                        if (denom > 0) {
                            double alpha = gap / denom;
                            alpha = Math.max(0.0, Math.min(1.0, alpha));
                            LH = LH_before - alpha * tc.tH();
                            LR = LR_before + alpha * tc.tR();
                        }
                        break;
                    }
                }
            } else {
                // Collaboration mode
                if (tc.tC() < 100000) {
                    double dh = tc.tH() - tc.tC();
                    double dr = tc.tC();

                    LH -= dh;
                    LR += dr;

                    // Condition (ii): robot capacity exceeded (rr + LR <= cycleTime)
                    if (rr + LR > cycleTime) {
                        if (dr > 0) {
                            double alpha = (cycleTime - rr - LR_before) / dr;
                            alpha = Math.max(0.0, Math.min(1.0, alpha));
                            LH = LH_before - alpha * dh;
                            LR = LR_before + alpha * dr;
                        }
                        break;
                    }

                    // Condition (iii): overshot balance point (rr+LR = rh+LH)
                    // rr + LR_before + alpha*dr = rh + LH_before - alpha*dh
                    // alpha*(dr + dh) = (rh + LH_before) - (rr + LR_before)
                    if (rr + LR > rh + LH) {
                        double gap = (rh + LH_before) - (rr + LR_before);
                        double denom = dh + dr;
                        if (denom > 0) {
                            double alpha = gap / denom;
                            alpha = Math.max(0.0, Math.min(1.0, alpha));
                            LH = LH_before - alpha * dh;
                            LR = LR_before + alpha * dr;
                        }
                        break;
                    }
                }
            }
        }

        return Math.max(0.0, Math.max(rh + LH, rr + LR) - currentMakespan);
    }
}
