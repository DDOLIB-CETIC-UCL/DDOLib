package org.ddolib.examples.ssalbrb;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.Relaxation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Relaxation for state representation <r_h, r_r, E> where E encodes both
 * assignment status and timing. Merging keeps optimistic (minimum) resource
 * availability and relaxed earliest start times.
 *
 * Precedence feasibility is enforced during state merging: after selecting the
 * top k* candidate tasks by frequency and completion time, a precedence closure
 * step iteratively adds any predecessor that is not yet in the selected set,
 * ensuring the merged state corresponds to a valid partial schedule.
 */
public class SSALBRBRelax implements Relaxation<SSALBRBState> {

    private final int[] humanDurations;
    private final int[] robotDurations;
    private final int[] collaborationDurations;
    private final Map<Integer, List<Integer>> predecessors;

    public SSALBRBRelax(int[] humanDurations, int[] robotDurations, int[] collaborationDurations,
                        Map<Integer, List<Integer>> predecessors) {
        this.humanDurations = humanDurations;
        this.robotDurations = robotDurations;
        this.collaborationDurations = collaborationDurations;
        this.predecessors = predecessors;
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

        // Compute k*: minimum number of assigned tasks across all merged states
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

        // Collect per-task statistics across all merged states
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

        // Step 1: Sort candidates by (assignedCount desc, bestAssignedE desc) and select top k*
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

        // Step 2: Precedence closure
        // If a selected task has a predecessor not yet selected, add it.
        // Repeat until no more predecessors need to be added.
        boolean changed = true;
        while (changed) {
            changed = false;
            for (int i = 0; i < n; i++) {
                if (!chosenAssigned[i]) continue;
                List<Integer> preds = predecessors.get(i);
                if (preds == null) continue;
                for (int pred : preds) {
                    if (!chosenAssigned[pred]) {
                        chosenAssigned[pred] = true;
                        changed = true;
                    }
                }
            }
        }

        // Compute max completion time among chosen assigned tasks (for resource consistency check)
        int maxCompletion = 0;
        for (int i = 0; i < n; i++) {
            if (chosenAssigned[i] && bestAssignedE[i] != Integer.MIN_VALUE) {
                maxCompletion = Math.max(maxCompletion, -bestAssignedE[i]);
            }
        }

        // Resource consistency adjustment
        int mergedHumanReady = bestHumanReady;
        int mergedRobotReady = bestRobotReady;
        if (Math.max(mergedHumanReady, mergedRobotReady) < maxCompletion) {
            if (mergedHumanReady <= mergedRobotReady) {
                mergedRobotReady = maxCompletion;
            } else {
                mergedHumanReady = maxCompletion;
            }
        }

        // Build merged E vector
        List<Integer> mergedE = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            if (chosenAssigned[i]) {
                // Assigned: use optimistic (least negative = earliest) completion time
                mergedE.add(bestAssignedE[i]);
            } else {
                // Unassigned: use optimistic (earliest) start time
                int est = bestUnassignedE[i];
                if (est == Integer.MAX_VALUE) {
                    est = 0;
                }
                // Further tighten EST using completion times from states where task was assigned
                if (assignedCount[i] > 0 && bestAssignedE[i] != Integer.MIN_VALUE) {
                    int completionTime = -bestAssignedE[i];
                    int maxDuration = Math.max(Math.max(humanDurations[i], robotDurations[i]), collaborationDurations[i]);
                    int earliestFromAssigned = Math.max(0, completionTime - maxDuration);
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
