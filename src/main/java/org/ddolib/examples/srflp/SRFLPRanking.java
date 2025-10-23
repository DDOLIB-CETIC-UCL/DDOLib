package org.ddolib.examples.srflp;


import org.ddolib.modeling.StateRanking;

/**
 * Implements a ranking between two {@link SRFLPState} instances for use in
 * decision diagram or search-based algorithms.
 *
 * <p>
 * The ranking is based on the number of departments that still need to be placed,
 * i.e., the sum of departments in the {@code must} and {@code maybe} sets of a state.
 * A state with fewer remaining departments is considered "better" (ranked lower).
 * </p>
 *
 * <p>
 * This class implements the {@link StateRanking} interface and can be used
 * to prioritize states during search, pruning, or frontier management in
 * SRFLP solvers.
 * </p>
 */
public class SRFLPRanking implements StateRanking<SRFLPState> {
    /**
     * Compares two SRFLP states based on the total number of remaining departments.
     *
     * @param o1 the first state to compare
     * @param o2 the second state to compare
     * @return a negative integer if {@code o1} has fewer remaining departments than {@code o2},
     *         zero if they have the same number of remaining departments,
     *         or a positive integer if {@code o1} has more remaining departments than {@code o2}.
     */
    @Override
    public int compare(SRFLPState o1, SRFLPState o2) {
        int total1 = o1.must().cardinality() + o1.maybe().cardinality();
        int total2 = o2.must().cardinality() + o2.maybe().cardinality();
        return Integer.compare(total1, total2);
    }
}
