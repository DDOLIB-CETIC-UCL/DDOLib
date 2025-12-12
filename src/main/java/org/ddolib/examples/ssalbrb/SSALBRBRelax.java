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

    @Override
    public SSALBRBState mergeStates(Iterator<SSALBRBState> states) {
        if (!states.hasNext()) {
            throw new IllegalArgumentException("Cannot merge empty state set");
        }

        int bestHumanReady = Integer.MAX_VALUE;
        int bestRobotReady = Integer.MAX_VALUE;
        List<Integer> mergedE = null;

        while (states.hasNext()) {
            SSALBRBState state = states.next();
            bestHumanReady = Math.min(bestHumanReady, state.humanAvailable());
            bestRobotReady = Math.min(bestRobotReady, state.robotAvailable());
            
            List<Integer> earliestStartTimes = state.earliestStartTimes();
            if (mergedE == null) {
                mergedE = new ArrayList<>(earliestStartTimes);
            } else {
                for (int i = 0; i < earliestStartTimes.size(); i++) {
                    int existing = mergedE.get(i);
                    int candidate = earliestStartTimes.get(i);
                    
                    // Relaxation logic:
                    // - If both unassigned (>= 0): take minimum (more optimistic)
                    // - If both assigned (< 0): take maximum completion time (less negative, later completion)
                    // - If mixed: prefer unassigned (more optimistic relaxation)
                    if (existing >= 0 && candidate >= 0) {
                        mergedE.set(i, Math.min(existing, candidate));
                    } else if (existing < 0 && candidate < 0) {
                        mergedE.set(i, Math.max(existing, candidate));
                    } else {
                        // Mixed: one assigned, one not -> mark as unassigned with earliest possible time
                        mergedE.set(i, Math.max(0, Math.max(existing, candidate)));
                    }
                }
            }
        }

        if (mergedE == null) {
            mergedE = List.of();
        }

        return new SSALBRBState(bestHumanReady, bestRobotReady, mergedE);
    }

    @Override
    public double relaxEdge(SSALBRBState from,
                            SSALBRBState to,
                            SSALBRBState merged,
                            Decision decision,
                            double originalCost) {
        return originalCost;
    }
}
