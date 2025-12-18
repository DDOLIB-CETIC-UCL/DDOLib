package org.ddolib.examples.ssalbrb;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.Relaxation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Relaxation for state representation <r_h, r_r, E> where E encodes both
 * assignment status and timing. Merging keeps optimistic (minimum) resource
 * availability and relaxed earliest start times.
 */
public class SSALBRBRelax implements Relaxation<SSALBRBState> {

    private final int[] humanDurations;
    private final int[] robotDurations;
    private final int[] collaborationDurations;

    public SSALBRBRelax(int[] humanDurations, int[] robotDurations, int[] collaborationDurations) {
        this.humanDurations = humanDurations;
        this.robotDurations = robotDurations;
        this.collaborationDurations = collaborationDurations;
    }

    @Override
    public SSALBRBState mergeStates(Iterator<SSALBRBState> states) {
        if (!states.hasNext()) {
            throw new IllegalArgumentException("Cannot merge empty state set");
        }

        List<SSALBRBState> stateList = new ArrayList<>();
        while (states.hasNext()) {
            stateList.add(states.next());
        }

        int bestHumanReady = Integer.MAX_VALUE;
        int bestRobotReady = Integer.MAX_VALUE;

        SSALBRBState first = stateList.get(0);
        int n = first.earliestStartTimes().size();

        int[] assignedCount = new int[n];
        int[] bestAssignedE = new int[n];
        int[] bestUnassignedE = new int[n];

        for (int i = 0; i < n; i++) {
            bestAssignedE[i] = Integer.MIN_VALUE;
            bestUnassignedE[i] = Integer.MAX_VALUE;
        }

        int k = Integer.MAX_VALUE;
        for (SSALBRBState state : stateList) {
            int cnt = 0;
            List<Integer> e = state.earliestStartTimes();
            for (int i = 0; i < n; i++) {
                if (e.get(i) < 0) {
                    cnt++;
                }
            }
            k = Math.min(k, cnt);
        }

        for (SSALBRBState state : stateList) {
            bestHumanReady = Math.min(bestHumanReady, state.humanAvailable());
            bestRobotReady = Math.min(bestRobotReady, state.robotAvailable());

            List<Integer> e = state.earliestStartTimes();
            for (int i = 0; i < n; i++) {
                int v = e.get(i);
                if (v < 0) {
                    assignedCount[i]++;
                    bestAssignedE[i] = Math.max(bestAssignedE[i], v);
                } else {
                    bestUnassignedE[i] = Math.min(bestUnassignedE[i], v);
                }
            }
        }

        boolean[] chosenAssigned = new boolean[n];
        int chosen = 0;

        List<Integer> candidates = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            if (assignedCount[i] > 0) {
                candidates.add(i);
            }
        }
        candidates.sort((a, b) -> {
            int cmp = Integer.compare(assignedCount[b], assignedCount[a]);
            if (cmp != 0) {
                return cmp;
            }
            return Integer.compare(bestAssignedE[b], bestAssignedE[a]);
        });

        for (int idx : candidates) {
            if (chosen >= k) {
                break;
            }
            chosenAssigned[idx] = true;
            chosen++;
        }

        int maxCompletion = 0;
        for (int i = 0; i < n; i++) {
            if (chosenAssigned[i] && bestAssignedE[i] != Integer.MIN_VALUE) {
                maxCompletion = Math.max(maxCompletion, -bestAssignedE[i]);
            }
        }

        int mergedHumanReady = bestHumanReady;
        int mergedRobotReady = bestRobotReady;
        if (Math.max(mergedHumanReady, mergedRobotReady) < maxCompletion) {
            if (mergedHumanReady <= mergedRobotReady) {
                mergedRobotReady = maxCompletion;
            } else {
                mergedHumanReady = maxCompletion;
            }
        }

        List<Integer> mergedE = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            if (chosenAssigned[i]) {
                mergedE.add(bestAssignedE[i]);
            } else {
                int est = bestUnassignedE[i];
                if (est == Integer.MAX_VALUE) {
                    est = 0;
                }
                if (assignedCount[i] > 0 && bestAssignedE[i] != Integer.MIN_VALUE) {
                    int completionTime = -bestAssignedE[i];
                    int minDuration = Math.max(Math.max(humanDurations[i], robotDurations[i]), collaborationDurations[i]);
                    int earliestFromAssigned = Math.max(0, completionTime - minDuration);
                    est = Math.min(est, earliestFromAssigned);
                }
                mergedE.add(est);
            }
        }

        return new SSALBRBState(mergedHumanReady, mergedRobotReady, mergedE);
    }

    @Override
    public double relaxEdge(SSALBRBState from,
                            SSALBRBState to,
                            SSALBRBState merged,
                            Decision decision,
                            double originalCost) {
        double relaxedCost = Math.max(0.0, ((double) merged.makespan()) - ((double) from.makespan()));
        return Math.min(originalCost, relaxedCost);
    }
}
