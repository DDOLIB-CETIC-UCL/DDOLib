package org.ddolib.examples.smic;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.Relaxation;

import java.util.BitSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
/**
 * The {@code SMICRelax} class implements a relaxation operator for the
 * {@link SMICProblem}, used in Decision Diagram Optimization (DDO)-based solvers.
 * <p>
 * The relaxation defines how to merge multiple {@link SMICState} instances
 * representing similar subproblems into a single aggregated state, in order
 * to reduce the diagram size while maintaining an admissible relaxation
 * (i.e., not underestimating the true cost).
 * </p>
 *
 * <p>
 * This specific relaxation merges states by:
 * </p>
 * <ul>
 *   <li>Taking the <b>union</b> of their remaining jobs,</li>
 *   <li>Taking the <b>minimum</b> of their current times (earliest time reached),</li>
 *   <li>Taking the <b>maximum</b> of their minimum inventory levels,</li>
 *   <li>Taking the <b>minimum</b> of their maximum inventory levels.</li>
 * </ul>
 * If the resulting inventory interval becomes infeasible
 * (i.e., {@code minCurrentInventory > maxCurrentInventory}),
 * the merged state uses the same value for both bounds
 * to ensure consistency.
 *
 * <p>
 * The {@link #relaxEdge(SMICState, SMICState, SMICState, Decision, double)} method
 * does not alter the transition cost — it returns the same value as the original edge,
 * meaning this relaxation focuses on state aggregation only.
 * </p>
 *
 * @see SMICState
 * @see SMICProblem
 * @see Relaxation
 */
public class SMICRelax implements Relaxation<SMICState> {
    /** The underlying problem instance associated with this relaxation. */
    final SMICProblem problem;

    /**
     * Constructs a relaxation operator for the given {@link SMICProblem}.
     *
     * @param problem the problem definition providing data and constraints
     */

    public SMICRelax(SMICProblem problem) {
        this.problem = problem;
    }
    /**
     * Merges several {@link SMICState} objects into a single relaxed state.
     * <p>
     * The merged state conservatively approximates the set of original states,
     * ensuring that no feasible solution is lost while potentially combining
     * multiple subproblems to reduce computational complexity.
     * </p>
     *
     * @param states an iterator over the states to be merged
     * @return a new relaxed {@link SMICState} combining the information of all inputs
     */
    @Override
    public SMICState mergeStates(final Iterator<SMICState> states) {
        BitSet remaining = new BitSet();
        int currentTime = Integer.MIN_VALUE;
        int minCurrentInventory = Integer.MAX_VALUE;
        int maxCurrentInventory = Integer.MIN_VALUE;
        while (states.hasNext()) {
            final SMICState state = states.next();
            remaining.or(state.remainingJobs());
            currentTime = Math.max(currentTime, state.currentTime());
            minCurrentInventory = Math.min(minCurrentInventory, state.minCurrentInventory());
            maxCurrentInventory = Math.max(maxCurrentInventory, state.maxCurrentInventory());
        }
        return new SMICState(remaining, currentTime, minCurrentInventory, maxCurrentInventory);
    }
    /**
     * Relaxes the cost of an edge between two states.
     * <p>
     * In this implementation, the relaxation does not modify the edge cost;
     * the returned cost is identical to the input cost.
     * </p>
     *
     * @param from   the source state before applying the decision
     * @param to     the target state after applying the decision
     * @param merged the merged state resulting from relaxation
     * @param d      the decision applied
     * @param cost   the original transition cost
     * @return the relaxed (possibly modified) edge cost, unchanged in this implementation
     */
    @Override
    public double relaxEdge(SMICState from, SMICState to, SMICState merged, Decision d, double cost) {
        return cost;
    }
}

