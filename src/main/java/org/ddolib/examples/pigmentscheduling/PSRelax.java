package org.ddolib.examples.pigmentscheduling;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.Relaxation;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static org.ddolib.examples.pigmentscheduling.PSProblem.IDLE;
/**
 * Implements the relaxation mechanism used in the DDO framework
 * for the Pigment Scheduling Problem (PSP).
 * <p>
 * The {@code PSRelax} class defines how multiple {@link PSState} objects
 * can be merged into a single relaxed state during the search process.
 * Relaxation is a key component of the DDO algorithm, enabling the merging
 * of similar or compatible states to reduce the size of the search graph
 * while preserving admissibility.
 * </p>
 *
 * <p>
 * In this implementation, the relaxation rule merges states by:
 * </p>
 * <ul>
 *     <li>Taking the earliest time index among all merged states;</li>
 *     <li>Taking the minimum value of {@code previousDemands} for each item type,
 *         effectively under-approximating remaining demands;</li>
 *     <li>Setting the machine to the {@link PSProblem#IDLE} state, as the
 *         specific pigment context is lost during merging.</li>
 * </ul>
 * <p>
 * The resulting relaxed state is less constrained (hence "relaxed"),
 * which allows the solver to reason over fewer states while maintaining
 * lower-bound consistency.
 * </p>
 *
 * @see PSProblem
 * @see PSState
 * @see Relaxation
 * @see PSDdoMain
 */
public class PSRelax implements Relaxation<PSState> {
    /** Reference to the Pigment Scheduling problem definition. */
    PSProblem problem;

    /**
     * Constructs a relaxation operator associated with a given PSP problem.
     *
     * @param problem the {@link PSProblem} instance that defines
     *                the scheduling parameters, costs, and demands.
     */
    public PSRelax(PSProblem problem) {
        this.problem = problem;
    }

    /**
     * Computes the set of item types that still have unsatisfied demands,
     * along with the currently produced item type (if any).
     *
     * @param state the {@link PSState} for which to extract the set of active items
     * @return a set of integers representing the indices of active or pending item types
     */
    private static Set<Integer> members(PSState state) {
        Set<Integer> mem = new HashSet<>();
        for (int i = 0; i < state.previousDemands.length; i++) {
            if (state.previousDemands[i] >= 0) {
                mem.add(i);
            }
        }
        if (state.next != -1) {
            mem.add(state.next);
        }
        return mem;
    }
    /**
     * Merges multiple PSP states into a single relaxed state.
     * <p>
     * The merging process:
     * </p>
     * <ul>
     *     <li>Takes the minimum time index among the given states;</li>
     *     <li>For each item type, takes the smallest (earliest) previous demand index;</li>
     *     <li>Sets the resulting state to the idle production mode.</li>
     * </ul>
     * This method effectively creates an under-approximation of the merged states,
     * representing a superset of their feasible continuations.
     * @param states an iterator over the {@link PSState} instances to be merged
     * @return a new relaxed {@link PSState} representing the merged configuration
     */
    @Override
    public PSState mergeStates(final Iterator<PSState> states) {
        PSState currState = states.next();
        int[] prevDemands = Arrays.copyOf(currState.previousDemands, currState.previousDemands.length);
        int time = currState.t;
        while (states.hasNext()) {
            PSState state = states.next();
            time = Math.min(time, state.t);
            for (int i = 0; i < prevDemands.length; i++) {
                prevDemands[i] = Math.min(prevDemands[i], state.previousDemands[i]);
            }
        }
        return new PSState(time, IDLE, prevDemands);

    }
    /**
     * Returns the relaxed transition cost between two PSP states.
     * <p>
     * In this simple implementation, the relaxation does not alter
     * the transition cost and simply returns the original value.
     * More advanced relaxations could, however, modify this cost
     * to tighten the lower bounds.
     * </p>
     *
     * @param from    the originating state
     * @param to      the destination state
     * @param merged  the merged (relaxed) state
     * @param d       the decision leading to the transition
     * @param cost    the original transition cost
     * @return the (possibly modified) transition cost after relaxation
     */

    @Override
    public double relaxEdge(PSState from, PSState to, PSState merged, Decision d, double cost) {
        return cost;
    }

    private long[] computeMST(int[][] changeover) {
        int n = changeover.length;
        long[] minEdge = new long[n];
        boolean[] inMST = new boolean[n];
        Arrays.fill(minEdge, Long.MAX_VALUE);
        minEdge[0] = 0; // Start from the first item
        long[] mstCost = new long[1 << n]; // To store the MST cost for each subset of nodes
        for (int i = 0; i < n; i++) {
            int u = -1;
            for (int j = 0; j < n; j++) {
                if (!inMST[j] && (u == -1 || minEdge[j] < minEdge[u])) {
                    u = j;
                }
            }
            inMST[u] = true;
            for (int v = 0; v < n; v++) {
                if (changeover[u][v] < minEdge[v]) {
                    minEdge[v] = changeover[u][v];
                }
            }
            // Update the MST cost for the current subset
            for (int mask = 0; mask < (1 << n); mask++) {
                if ((mask & (1 << u)) == 0) {
                    mstCost[mask | (1 << u)] = Math.min(mstCost[mask | (1 << u)], mstCost[mask] + minEdge[u]);
                }
            }
        }
        return mstCost;
    }

}