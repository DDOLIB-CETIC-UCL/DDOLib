package org.ddolib.examples.gruler;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.Relaxation;

import java.util.BitSet;
import java.util.Iterator;
/**
 * Relaxation operator for the Golomb Ruler (GR) problem.
 * <p>
 * This class defines how multiple search states ({@link GRState}) can be merged
 * to create a relaxed (i.e., aggregated) state during search algorithms such as
 * DDO (Decision Diagram Optimization) or Anytime Column Search.
 * </p>
 *
 * <p>
 * The relaxation used here computes the intersection of the sets of marks and
 * distances present in the input states. This ensures that only marks and
 * distances common to all states are kept in the merged state.
 * The resulting state represents a conservative approximation that does not
 * introduce new distances, preserving feasibility.
 * </p>
 *
 * <p>
 * The last mark in the merged state is the minimum of all last marks
 * across the input states, ensuring consistency with the most constrained (shortest) partial ruler.
 * </p>
 *
 * <p><b>Example:</b></p>
 * <pre>{@code
 * GRRelax relax = new GRRelax();
 * GRState merged = relax.mergeStates(List.of(state1, state2).iterator());
 * }</pre>
 *
 * @see GRState
 * @see Relaxation
 */
public class GRRelax implements Relaxation<GRState> {
    /**
     * Merges several {@link GRState} objects into a single relaxed state.
     * <p>
     * The resulting state contains:
     *  </p>
     * <ul>
     *     <li>The intersection of all mark sets (only marks present in all states are kept).</li>
     *     <li>The intersection of all distance sets (only distances present in all states are kept).</li>
     *     <li>The smallest {@code lastMark} value among all merged states.</li>
     * </ul>
     * @param states an iterator over the states to merge.
     * @return a new {@link GRState} representing the relaxed (merged) state.
     */
    @Override
    public GRState mergeStates(final Iterator<GRState> states) {
        // take the intersection of the marks and distances sets
        GRState curr = states.next();
        BitSet intersectionMarks = (BitSet) curr.getMarks().clone();
        BitSet intersectionDistances = (BitSet) curr.getDistances().clone();
        int lastMark = curr.getLastMark();
        while (states.hasNext()) {
            GRState state = states.next();
            intersectionMarks.and(state.getMarks());
            intersectionDistances.and(state.getDistances());
            lastMark = Math.min(lastMark, state.getLastMark());
        }
        return new GRState(intersectionMarks, intersectionDistances, lastMark);
    }
    /**
     * Computes the relaxed cost of transitioning between two states in the relaxed problem.
     * <p>
     * In this implementation, the relaxation does not modify the cost — it simply returns
     * the same value as the original transition cost.
     * </p>
     *
     * @param from   the source state before the transition.
     * @param to     the destination state after the transition.
     * @param merged the merged relaxed state (unused in this relaxation).
     * @param d      the decision made for the transition.
     * @param cost   the original transition cost.
     * @return the relaxed transition cost (equal to {@code cost}).
     */
    @Override
    public double relaxEdge(GRState from, GRState to, GRState merged, Decision d, double cost) {
        return cost;
    }
}
