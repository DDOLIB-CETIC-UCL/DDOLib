package org.ddolib.examples.pigmentscheduling;

import org.ddolib.modeling.StateRanking;

import java.util.Arrays;
/**
 * Provides a ranking heuristic for comparing two {@link PSState} objects
 * within the Production Scheduling Problem (PSP) search framework.
 * <p>
 * The {@code PSRanking} class implements the {@link StateRanking} interface
 * and defines a simple heuristic ordering between states based on
 * their total remaining demand.
 * This ranking is typically used by search algorithms such as DDO
 * (Decision Diagram Optimization) to prioritize which states
 * should be expanded first during the exploration of the search tree.
 * </p>
 *
 * <p>
 * In this implementation, states with a smaller total demand are considered
 * "better" (i.e., ranked higher), as they represent configurations that are
 * closer to satisfying all production demands.
 * </p>
 *
 * <p>
 * Note: this ranking heuristic is a simple and potentially suboptimal choice.
 * More advanced heuristics may consider additional aspects such as
 * stocking cost accumulation, changeover penalties, or temporal feasibility.
 * </p>
 *
 * @see PSState
 * @see StateRanking
 */
public class PSRanking implements StateRanking<PSState> {
    /**
     * Compares two PSP states based on their total remaining demand.
     * <p>
     * The state with the smaller sum of {@code previousDemands} values
     * (i.e., fewer remaining demands) is considered to have a better rank.
     * </p>
     *
     * @param s1 the first {@link PSState} to compare
     * @param s2 the second {@link PSState} to compare
     * @return a negative integer if {@code s1} is better than {@code s2},
     *         zero if they are equivalent, or a positive integer otherwise
     */
    @Override
    public int compare(PSState s1, PSState s2) {
        // the state with the smallest total demand is the best (not sure about this)
        int totS1 = Arrays.stream(s1.previousDemands).sum();
        int totS2 = Arrays.stream(s2.previousDemands).sum();
        return Integer.compare(totS1, totS2);
    }
}
