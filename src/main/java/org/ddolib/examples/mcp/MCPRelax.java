package org.ddolib.examples.mcp;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.Relaxation;

import java.util.ArrayList;
import java.util.Iterator;

import static java.lang.Integer.*;
import static java.lang.Math.abs;

/**
 * Implements a relaxation strategy for the <b>Maximum Cut Problem (MCP)</b>.
 * <p>
 * This relaxation is used in dynamic programming or branch-and-bound algorithms to
 * merge multiple states into a single optimistic state while preserving bounds.
 * </p>
 *
 * <p>
 * The merging strategy works as follows:
 * </p>
 * <ul>
 *     <li>If all net benefits for a variable are positive, the merged state keeps the smallest value.</li>
 *     <li>If all net benefits for a variable are negative, the merged state keeps the largest value.</li>
 *     <li>If net benefits have mixed signs, the merged value is set to 0.</li>
 * </ul>
 *
 * <p>
 * The {@link #relaxEdge(MCPState, MCPState, MCPState, Decision, double)} method adjusts
 * the transition cost to ensure the relaxation remains an over-approximation of the
 * true cost.
 * </p>
 *
 * @see MCPProblem
 * @see MCPState
 * @see Relaxation
 */
public class MCPRelax implements Relaxation<MCPState> {

    /**
     * The MCP problem instance for which this relaxation is applied.
     */
    final MCPProblem problem;

    /**
     * Constructs a relaxation for the given MCP problem.
     *
     * @param problem the MCP problem instance
     */
    public MCPRelax(MCPProblem problem) {
        this.problem = problem;
    }
    /**
     * Merges multiple MCP states into a single optimistic state.
     * <p>
     * The merged state keeps a conservative estimate of net benefits for remaining
     * decision variables in order to maintain an over-approximation of the optimal solution.
     * </p>
     *
     * @param states an iterator over states to merge
     * @return a new {@link MCPState} representing the merged state
     */
    @Override
    public MCPState mergeStates(Iterator<MCPState> states) {
        MCPState state = states.next();
        ArrayList<Integer> merged = new ArrayList<>(state.netBenefit());
        int depth = state.depth();

        while (states.hasNext()) {
            MCPState current = states.next();
            for (int i = depth; i < problem.nbVars(); i++) {
                Integer mergedI = merged.get(i);
                Integer currentI = current.netBenefit().get(i);


                if (signum(mergedI) == 1 && signum(currentI) == 1) {
                    //If all the net benefits are positive, we keep the smallest one
                    merged.set(i, min(mergedI, currentI));
                } else if (signum(mergedI) == -1 && signum(currentI) == -1) {
                    // If all the net benefits are negative, we keep the biggest one
                    merged.set(i, max(mergedI, currentI));
                } else {
                    // Otherwise, we set at 0
                    merged.set(i, 0);
                }
            }
        }
        return new MCPState(merged, depth);
    }
    /**
     * Computes the relaxed transition cost from one state to another given a merged state.
     * <p>
     * This method adjusts the cost to account for the differences between the actual net
     * benefits in the target state and the merged state, ensuring that the relaxation
     * remains optimistic.
     * </p>
     *
     * @param from the initial state
     * @param to the target state
     * @param merged the merged state used for relaxation
     * @param d the decision applied to reach the target state
     * @param cost the original transition cost
     * @return the relaxed transition cost
     */

    @Override
    public double relaxEdge(MCPState from, MCPState to, MCPState merged, Decision d, double cost) {
        double toReturn = -cost;
        for (int i = d.var() + 1; i < problem.nbVars(); i++) {
            toReturn += abs(to.netBenefit().get(i)) - abs(merged.netBenefit().get(i));
        }
        return -toReturn;
    }
}
