package org.ddolib.examples.maximumcoverage;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.Relaxation;

import java.util.BitSet;
import java.util.Iterator;
/**
 * Relaxation operator for {@link MaxCoverState} in the Maximum Coverage problem.
 *
 * <p>
 * This class implements {@link Relaxation} and defines how multiple states
 * can be merged into a single relaxed state. It is typically used in
 * Decision Diagram Optimization (DDO) to reduce the size of the diagram
 * while maintaining an admissible relaxation.
 *
 * <p>
 * The relaxation merges states by computing the intersection of their covered
 * items. Additional statistics (total intersection size, number of merges, and
 * number of zero intersections) are tracked for analysis or debugging purposes.
 */
public class    MaxCoverRelax implements Relaxation<MaxCoverState> {
    /** The MaxCover problem instance associated with this relaxation. */
    final MaxCoverProblem problem;
    /** Sum of the cardinalities of intersections computed during merges. */
    double totInsersectionSize = 0;
    /** Number of merge operations performed. */
    int nbMerge = 0;
    /** Number of merges that resulted in an empty intersection. */
    int nbZeroIntersection = 0;
    /**
     * Constructs a MaxCover relaxation operator for a given problem instance.
     *
     * @param problem the MaxCover problem instance
     */
    public MaxCoverRelax(MaxCoverProblem problem) {
        this.problem = problem;
    }
    /**
     * Merges a collection of states into a single relaxed state.
     *
     * <p>
     * The merged state covers only the items that are common to all input states
     * (intersection). This ensures the relaxation is admissible, as it does not
     * overestimate coverage.
     *
     * @param states an iterator over the states to merge
     * @return a new {@link MaxCoverState} representing the intersection of the input states
     */
    @Override
    public MaxCoverState mergeStates(final Iterator<MaxCoverState> states) {
        MaxCoverState state = states.next();
        BitSet intersectionCoveredItems = (BitSet) state.coveredItems().clone();
        int nbStatesMerged = 0;
        while (states.hasNext()) {
            state = states.next();
            intersectionCoveredItems.and(state.coveredItems());
            nbStatesMerged++;
        }


        int intersectionCard = intersectionCoveredItems.cardinality();
        totInsersectionSize += intersectionCard;
        nbMerge++;
        if (intersectionCard == 0) {
            nbZeroIntersection++;
        }
        return new MaxCoverState(intersectionCoveredItems);
    }
    /**
     * Computes the relaxed cost of a transition between states.
     *
     * <p>
     * In this implementation, the cost is not modified by the relaxation and
     * is returned as-is.
     *
     * @param from the source state
     * @param to the destination state
     * @param merged the merged state after relaxation
     * @param d the decision applied
     * @param cost the original transition cost
     * @return the relaxed transition cost (same as input cost)
     */
    @Override
    public double relaxEdge(MaxCoverState from, MaxCoverState to, MaxCoverState merged, Decision d, double cost) {
        return cost;
    }
}
