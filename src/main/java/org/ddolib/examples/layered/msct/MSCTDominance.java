package org.ddolib.examples.layered.msct;

import java.util.Set;
import org.ddolib.layered.modeling.Dominance;

/**
 * Implements the dominance relation for the Maximum Sum of Compatible Tasks (MSCT) problem.
 *
 * <p>This class defines how two states of the MSCT problem can be compared in terms of dominance.
 * In this context, a state {@code state1} is said to be <i>dominated or equal</i> to another state
 * {@code state2} if both states have the same set of remaining jobs to schedule, and {@code state2}
 * achieves the same or an earlier current time (i.e., a better schedule in terms of completion
 * time).
 *
 * <p>The dominance relation is used in search algorithms (such as DDO or A*) to prune suboptimal
 * states — if one state dominates another, the dominated state can be safely discarded without
 * affecting optimality.
 *
 * <p><b>Example:</b>
 *
 * <pre>
 * MSCTState s1 = new MSCTState(remainingJobs1, 10);
 * MSCTState s2 = new MSCTState(remainingJobs1, 8);
 *
 * MSCTDominance dominance = new MSCTDominance();
 * // true, since s2.currentTime() is less than s1.currentTime()
 * boolean result = dominance.isDominatedOrEqual(s1, s2);
 * </pre>
 */
public class MSCTDominance implements Dominance<MSCTState> {

    /** Creates a new instance of this dominance relation. */
    public MSCTDominance() {}

    /**
     * Returns a key used for grouping states before applying dominance checks.
     *
     * <p>Only states with the same set of remaining jobs can dominate each other (see {@link
     * #isDominatedOrEqual}), so this set is the key: each state is only compared with the states
     * having the same remaining jobs, instead of all the states of its layer.
     *
     * @param state the state from which to extract the key
     * @return the set of remaining jobs of the state
     */
    @Override
    public Set<Integer> getKey(MSCTState state) {
        return state.remainingJobs();
    }

    /**
     * Checks whether the first state {@code state1} is dominated or equal to the second state
     * {@code state2}.
     *
     * <p>A state {@code state1} is dominated by {@code state2} if:
     *
     * <ul>
     *   <li>Both states have the same set of remaining jobs to schedule, and
     *   <li>The current time of {@code state2} is less than or equal to that of {@code state1}.
     * </ul>
     *
     * <p>This indicates that {@code state2} represents a better (or equivalent) scheduling
     * situation, making {@code state1} redundant in the search process.
     *
     * @param state1 the first state to test for dominance
     * @param state2 the second state, potentially dominating the first
     * @return {@code true} if {@code state1} is dominated or equal to {@code state2}; {@code false}
     *     otherwise
     */
    @Override
    public boolean isDominatedOrEqual(MSCTState state1, MSCTState state2) {
        return state1.remainingJobs().equals(state2.remainingJobs())
                && state2.currentTime() <= state1.currentTime();
    }
}
