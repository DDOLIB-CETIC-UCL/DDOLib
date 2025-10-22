package org.ddolib.examples.pdp;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.Relaxation;

import java.util.BitSet;
import java.util.Iterator;
/**
 * Implements a relaxation operator for the Pickup and Delivery Problem (PDP) states.
 * <p>
 * This class is used in decision diagram-based algorithms (e.g., DDO) to merge
 * multiple states into a single relaxed state, thereby reducing the search space
 * while maintaining a conservative approximation of the solution.
 * </p>
 *
 * <p>
 * The relaxation is performed by taking the union of the sets of nodes to visit
 * and the current nodes from the input states. The minimum and maximum vehicle
 * content are adjusted accordingly to cover all merged states.
 * </p>
 *
 * <p>
 * The {@link #relaxEdge(PDPState, PDPState, PDPState, Decision, double)} method
 * currently does not modify the edge cost and simply returns the original cost.
 * </p>
 *
 * <p>
 * This class implements the {@link Relaxation} interface and can be plugged into
 * any PDP-based model that requires state relaxation for approximate or exact search.
 * </p>
 */
class PDPRelax implements Relaxation<PDPState> {
    private final PDPProblem problem;
    /**
     * Constructs a PDPRelax object for a given PDP problem instance.
     *
     * @param problem the PDP problem instance used for state information and constraints
     */
    public PDPRelax(PDPProblem problem) {
        this.problem = problem;
    }
    /**
     * Merges multiple PDP states into a single relaxed state.
     * <p>
     * The merged state represents the union of the input states, allowing the
     * algorithm to consider multiple possible states as a single relaxed node.
     * </p>
     *
     * @param states an iterator over the PDP states to be merged
     * @return a new {@link PDPState} representing the merged relaxation
     */
    @Override
    public PDPState mergeStates(final Iterator<PDPState> states) {
        //NB: the current node is normally the same in all states
        BitSet openToVisit = new BitSet(problem.n);
        BitSet current = new BitSet(problem.n);
        BitSet allToVisit = new BitSet(problem.n);
        int minContent = Integer.MAX_VALUE;
        int maxContent = Integer.MIN_VALUE;

        while (states.hasNext()) {
            PDPState state = states.next();
            //take the union; loose precision here
            openToVisit.or(state.openToVisit);
            allToVisit.or(state.allToVisit);
            current.or(state.current);
            minContent = Math.min(minContent, state.minContent);
            maxContent = Math.max(maxContent, state.maxContent);
        }
        //the heuristics is reset to the initial sorted edges and will be filtered again from scratch
        return new PDPState(current, openToVisit, allToVisit,minContent,maxContent);
    }
    /**
     * Relaxes the cost of an edge between two states.
     * <p>
     * Currently, this implementation returns the original edge cost without modification.
     * </p>
     *
     * @param from the origin PDP state
     * @param to the destination PDP state
     * @param merged the relaxed merged state
     * @param d the decision taken along the edge
     * @param cost the original cost of the edge
     * @return the relaxed cost of the edge (currently identical to {@code cost})
     */
    @Override
    public double relaxEdge(PDPState from, PDPState to, PDPState merged, Decision d, double cost) {
        return cost;
    }

}
