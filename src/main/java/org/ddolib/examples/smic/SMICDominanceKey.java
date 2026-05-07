package org.ddolib.examples.smic;

import java.util.BitSet;

/**
 * A record that represents a dominance key for grouping states in the
 * <b>Single Machine with Inventory Constraint (SMIC)</b> problem.
 * <p>
 * This key is used to group comparable states that share the same set of
 * remaining jobs. States with the same dominance key can be compared to
 * determine dominance relations, which allows efficient pruning of
 * suboptimal search nodes.
 * </p>
 *
 * @param remainingJobs a {@link BitSet} representing the set of jobs
 *                      that still need to be processed
 *
 * @see SMICDominance
 * @see SMICState
 */
public record SMICDominanceKey(BitSet remainingJobs) {
    /**
     * Returns a string representation of this dominance key.
     *
     * @return a string describing the remaining jobs in this key
     */
    @Override
    public String toString() {return "remaining jobs: " + remainingJobs;}
}
