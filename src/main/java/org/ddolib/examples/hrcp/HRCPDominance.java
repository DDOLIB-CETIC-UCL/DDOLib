package org.ddolib.examples.hrcp;

import org.ddolib.modeling.Dominance;

/**
 * Dominance relation for the HRCP scheduling problem.
 * <p>
 * Two states are comparable only when they have the same scheduled-task bitmask
 * (returned as the dominance key).  Among states with the same key, state {@code s1}
 * is dominated by {@code s2} when:
 * <ul>
 *     <li>{@code s1.tH >= s2.tH}</li>
 *     <li>{@code s1.tR >= s2.tR}</li>
 *     <li>For every unscheduled task {@code k}: {@code s1.readiness[k] >= s2.readiness[k]}</li>
 * </ul>
 * Under these conditions every feasible continuation from {@code s1} also exists
 * from {@code s2} with equal or better timing, so {@code s1} can be pruned.
 */
public class HRCPDominance implements Dominance<HRCPState> {

    /**
     * Returns the scheduled bitmask as dominance key so that only states with the
     * same set of completed tasks are ever compared.
     */
    @Override
    public Long getKey(HRCPState state) {
        return state.scheduled;
    }

    @Override
    public boolean isDominatedOrEqual(HRCPState s1, HRCPState s2) {
        if (s1.tH < s2.tH || s1.tR < s2.tR) return false;
        for (int k = 0; k < s1.readiness.length; k++) {
            if ((s1.scheduled & (1L << k)) == 0) {   // unscheduled
                if (s1.readiness[k] < s2.readiness[k]) return false;
            }
        }
        return true;
    }
}

