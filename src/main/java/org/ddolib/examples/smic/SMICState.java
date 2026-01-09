package org.ddolib.examples.smic;


import java.util.BitSet;

/**
 * The {@code SMICState} record represents a state in the search space of the
 * Single Machine with Inventory Constraint (SMIC) scheduling problem.
 * <p>
 * Each state encodes the current progress of the scheduling process, including:
 * </p>
 * <ul>
 *     <li>The set of jobs that remain to be scheduled,</li>
 *     <li>The current simulation time,</li>
 *     <li>The minimum and maximum possible inventory levels, used to handle relaxations
 *         and dominance checks in state-space search algorithms.</li>
 * </ul>
 *
 * <p>
 * This record is immutable and is typically used in algorithms such as
 * <b>Decision Diagram Optimization (DDO)</b>, <b>Anytime Column Search (ACS)</b>,
 * or other <b>branch-and-bound</b> based solvers to represent nodes of the search tree
 * or diagram layers.
 * </p>
 *
 * <h2>Example Usage</h2>
 * <pre>{@code
 * Set<Integer> remaining = Set.of(1, 2, 3);
 * SMICState state = new SMICState(remaining, 10, 5, 15);
 * System.out.println(state);
 * }</pre>
 *
 * <p>Output:</p>
 * <pre>
 * RemainingJobs [1, 2, 3] ----> currentTime 10 ---> minCurrentInventory5 ---> maxCurrentInventory15
 * </pre>
 *
 * @param remainingJobs        the set of job indices that remain to be scheduled
 * @param currentTime          the current time in the schedule
 * @param minCurrentInventory  the minimum possible inventory level at this state
 * @param maxCurrentInventory  the maximum possible inventory level at this state
 *
 * @see SMICProblem
 * @see SMICRelax
 * @see SMICRanking
 * @see SMICDominance
 */
public record SMICState(BitSet remainingJobs,
                        int currentTime,
                        int minCurrentInventory,
                        int maxCurrentInventory) {
    /**
     * Returns a string representation of this state, displaying the set of
     * remaining jobs, the current time, and the current inventory bounds.
     *
     * @return a formatted string describing the state
     */
    @Override
    public String toString() {
        return "RemainingJobs : " + remainingJobs + " ----> currentTime : " + currentTime + " ---> minCurrentInventory : " + minCurrentInventory + " ---> maxCurrentInventory : " + maxCurrentInventory;
    }

}


