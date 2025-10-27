package org.ddolib.examples.msct;


import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.Relaxation;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
/**
 * Implements the relaxation operator for the {@link MSCTProblem}
 * in the context of <b>Decision Diagram Optimization (DDO)</b> algorithms.
 * <p>
 * The relaxation defines how multiple {@link MSCTState} objects
 * (representing partial scheduling solutions) can be merged
 * into a single abstract state in order to control the width
 * of the decision diagram and maintain computational tractability.
 * </p>
 *
 * <p><b>Relaxation principle:</b></p>
 * <ul>
 *   <li>The merged state’s set of remaining jobs is the <b>union</b> of all jobs
 *       that remain in the input states.</li>
 *   <li>The merged state’s current time is the <b>minimum</b> of the current times
 *       across all merged states. This heuristic keeps the relaxation optimistic
 *       (i.e., it underestimates the completion times).</li>
 * </ul>
 *
 * <p>
 * This relaxation is typically used in DDO to merge similar states that share
 * overlapping sets of remaining jobs, reducing diagram width while ensuring
 * that no feasible solution is lost (i.e., maintaining validity of the lower bound).
 * </p>
 *
 * @see MSCTProblem
 * @see MSCTState
 * @see Relaxation
 * @see MSCTDdoMain
 */
public class MSCTRelax implements Relaxation<MSCTState> {
    /** The scheduling problem instance associated with this relaxation. */
    final MSCTProblem problem;

    /**
     * Constructs a relaxation operator for the given MSCT problem instance.
     *
     * @param problem the instance of the {@link MSCTProblem} to which this relaxation applies.
     */

    public MSCTRelax(MSCTProblem problem) {
        this.problem = problem;
    }
    /**
     * Merges several {@link MSCTState} objects into a single relaxed state.
     * <p>
     * The resulting state represents an optimistic combination of the input states:
     * it includes all remaining jobs that could appear in any of them, and uses
     * the minimum current time among them to avoid overestimating the cost.
     * </p>
     *
     * @param states an iterator over the states to merge.
     * @return a new {@link MSCTState} representing the relaxed merged state.
     */
    @Override
    public MSCTState mergeStates(final Iterator<MSCTState> states) {
        Set<Integer> unionJobs = new HashSet<>();
        int minCurrentTime = Integer.MAX_VALUE;
        while (states.hasNext()) {
            final MSCTState state = states.next();
            unionJobs.addAll(state.remainingJobs());
            minCurrentTime = Math.min(state.currentTime(), minCurrentTime);
        }
        return new MSCTState(unionJobs, minCurrentTime);
    }
    /**
     * Computes the relaxed cost associated with transitioning from one state to another.
     * <p>
     * In this implementation, no additional relaxation is applied to the edge cost;
     * the cost remains identical to the original transition cost.
     * </p>
     *
     * @param from   the origin state.
     * @param to     the target state.
     * @param merged the merged (relaxed) state resulting from the combination of states.
     * @param d      the decision made during the transition.
     * @param cost   the original transition cost.
     * @return the relaxed transition cost (identical to {@code cost} here).
     */
    @Override
    public double relaxEdge(MSCTState from, MSCTState to, MSCTState merged, Decision d, double cost) {
        return cost;
    }
}
