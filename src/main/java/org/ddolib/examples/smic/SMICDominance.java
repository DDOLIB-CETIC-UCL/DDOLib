package org.ddolib.examples.smic;

import org.ddolib.modeling.Dominance;
import org.ddolib.common.dominance.SimpleDominanceChecker;
/**
 * The {@code SMICDominance} class defines the dominance relation between two
 * states of the {@link SMICState} in the context of the
 * <b>Single Machine with Inventory Constraint (SMIC)</b> problem.
 * <p>
 * Dominance relations are used by search algorithms (such as DDO, A*, or ACS)
 * to prune suboptimal or redundant states during the exploration process.
 * A state {@code s1} is said to be dominated by another state {@code s2}
 * if {@code s2} represents an equivalent or better configuration of the system
 * according to a defined dominance rule.
 * </p>
 *
 * <p>
 * In this implementation, the dominance rule is defined as follows:
 * </p>
 * <ul>
 *     <li>Two states are compared only if they have the same set of remaining jobs;</li>
 *     <li>Among such states, the one with the smaller (or equal) current time
 *         dominates the other, since it reaches the same configuration earlier
 *         or at the same time.</li>
 * </ul>
 *
 *
 * <p>
 * This criterion allows pruning of states that would take longer to reach the same
 * remaining set of jobs, improving search efficiency without losing optimality.
 * </p>
 *
 * @see SMICState
 * @see Dominance
 * @see SimpleDominanceChecker
 * @see SMICDdoMain
 */
public class SMICDominance implements Dominance<SMICState> {
    /**
     * Returns a key used to group comparable states.
     * <p>
     * In this implementation, all states share the same key ({@code 0}),
     * meaning that any pair of states can potentially be compared for dominance.
     * </p>
     *
     * @param state the state for which the grouping key is computed
     * @return always {@code 0}, as no grouping distinction is applied
     */
    @Override
    public Integer getKey(SMICState state) {
        return 0;
    }
    /**
     * Determines whether one state is dominated by or equal to another.
     * <p>
     * A state {@code state1} is considered dominated (or equivalent) to
     * {@code state2} if:
     * </p>
     * <ul>
     *   <li>Both states have the same set of remaining jobs to process;</li>
     *   <li>The current time of {@code state2} is less than or equal to that of {@code state1}.</li>
     * </ul>
     * This ensures that the search does not revisit slower or redundant configurations.
     * @param state1 the state being tested for dominance
     * @param state2 the state potentially dominating {@code state1}
     * @return {@code true} if {@code state1} is dominated by or equal to {@code state2},
     *         {@code false} otherwise
     */

    @Override
    public boolean isDominatedOrEqual(SMICState state1, SMICState state2) {
        if (state1.remainingJobs().equals(state2.remainingJobs()) && state2.currentTime() <= state1.currentTime()) {
            return true;
        }
        return false;
    }
}
