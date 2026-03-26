package org.ddolib.examples.hrc;

import org.ddolib.modeling.Dominance;

/**
 * Dominance relation for the Human-Robot Collaboration scheduling problem.
 * <p>
 * A state {@code s1 = (tH1, tR1)} is dominated by {@code s2 = (tH2, tR2)} when
 * {@code tH1 >= tH2} and {@code tR1 >= tR2}. In that case every feasible
 * continuation from {@code s1} yields a makespan at least as large as the same
 * continuation from {@code s2}, so {@code s1} can be safely discarded.
 * </p>
 */
public class HRCDominance implements Dominance<HRCState> {

    /**
     * Returns a single dominance key for all states, meaning every pair of states
     * at the same layer is compared.
     */
    @Override
    public Integer getKey(HRCState state) {
        return 0;
    }

    /**
     * Returns {@code true} if {@code state1} is dominated by or equal to {@code state2}.
     * <p>
     * A state with larger (or equal) accumulated human <em>and</em> robot times is
     * dominated, because the resulting makespan can only be larger.
     * </p>
     */
    @Override
    public boolean isDominatedOrEqual(HRCState state1, HRCState state2) {
        return state1.tH() >= state2.tH() && state1.tR() >= state2.tR();
    }
}

