package org.ddolib.examples.msct;

import org.ddolib.modeling.Dominance;
/**
 * Implements the dominance relation for the Maximum Sum of Compatible Tasks (MSCT) problem.
 * <p>
 * This class defines how two states of the MSCT problem can be compared in terms of dominance.
 * In this context, a state {@code state1} is said to be <i>dominated or equal</i> to another
 * state {@code state2} if both states have the same set of remaining jobs to schedule,
 * and {@code state2} achieves the same or an earlier current time (i.e., a better schedule
 * in terms of completion time).
 * </p>
 *
 * <p>
 * The dominance relation is used in search algorithms (such as DDO or A*) to prune
 * suboptimal states — if one state dominates another, the dominated state can be safely
 * discarded without affecting optimality.
 * </p>
 *
 * <p><b>Example:</b></p>
 * <pre>
 * MSCTState s1 = new MSCTState(remainingJobs1, 10);
 * MSCTState s2 = new MSCTState(remainingJobs1, 8);
 *
 * MSCTDominance dominance = new MSCTDominance();
 * boolean result = dominance.isDominatedOrEqual(s1, s2); // true, since s2.currentTime() is less than s1.currentTime()
 * </pre>
 */
public class MSCTDominance implements Dominance<MSCTState> {
    /**
     * Returns a key used for grouping states before applying dominance checks.
     * <p>
     * In this simple implementation, all states share the same key (always {@code 0}),
     * meaning that the dominance check can potentially compare any two states.
     * This can be customized for efficiency in larger problems.
     * </p>
     *
     * @param state the state from which to extract the key.
     * @return an integer key representing the group of comparable states (always {@code 0} here).
     */
    @Override
    public Integer getKey(MSCTState state) {
        return 0;
    }
    /**
     * Checks whether the first state {@code state1} is dominated or equal to the second state {@code state2}.
     * <p>
     * A state {@code state1} is dominated by {@code state2} if:
     * </p>
     * <ul>
     *   <li>Both states have the same set of remaining jobs to schedule, and</li>
     *   <li>The current time of {@code state2} is less than or equal to that of {@code state1}.</li>
     * </ul>
     * This indicates that {@code state2} represents a better (or equivalent) scheduling situation,
     * making {@code state1} redundant in the search process.
     *
     * @param state1 the first state to test for dominance.
     * @param state2 the second state, potentially dominating the first.
     * @return {@code true} if {@code state1} is dominated or equal to {@code state2}; {@code false} otherwise.
     */
    @Override
    public boolean isDominatedOrEqual(MSCTState state1, MSCTState state2) {
        if (state1.remainingJobs().equals(state2.remainingJobs()) && state2.currentTime() <= state1.currentTime()) {
            return true;
        }
        return false;
    }
}
