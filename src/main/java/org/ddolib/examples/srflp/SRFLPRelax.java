package org.ddolib.examples.srflp;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.Relaxation;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Iterator;

import static java.lang.Integer.max;
import static java.lang.Integer.min;

/**
 * Implementation of a relaxation for the Single Row Facility Layout Problem (SRFLP).
 *
 * <p>
 * In this relaxation, multiple SRFLP states can be merged into a single state
 * to reduce the search space. The merged state over-approximates the possible
 * states by combining the "must" and "maybe" sets and taking minimal cut values.
 * </p>
 *
 * <p>
 * This class implements the {@link Relaxation} interface and is typically used
 * in decision diagram or DDO-based algorithms for SRFLP to enable state merging
 * and efficient pruning.
 * </p>
 */
public class SRFLPRelax implements Relaxation<SRFLPState> {

    private final SRFLPProblem problem;

    /**
     * Constructs a new relaxation instance for a given SRFLP problem.
     *
     * @param problem The SRFLP problem instance to which this relaxation applies.
     */
    public SRFLPRelax(SRFLPProblem problem) {
        this.problem = problem;

    }

    /**
     * Merges multiple SRFLP states into a single relaxed state.
     *
     * <p>
     * The merged state:
     * </p>
     * <ul>
     *     <li>Intersects the "must" sets of all input states.</li>
     *     <li>Unites the "must" and "maybe" sets into the merged "maybe" set.</li>
     *     <li>Takes the minimal cut values for each department.</li>
     *     <li>Uses the maximum depth among all merged states.</li>
     * </ul>
     *
     * @param states An iterator over the states to merge.
     * @return A new SRFLPState representing the merged relaxation of the input states.
     */
    @Override
    public SRFLPState mergeStates(Iterator<SRFLPState> states) {
        BitSet mergedMust = new BitSet(problem.nbVars());
        mergedMust.set(0, problem.nbVars(), true);
        BitSet mergedMaybes = new BitSet(problem.nbVars());
        int[] mergedCut = new int[problem.nbVars()];
        Arrays.fill(mergedCut, Integer.MAX_VALUE);
        int mergedDepth = 0;

        while (states.hasNext()) {
            SRFLPState state = states.next();
            mergedMust.and(state.must());
            mergedMaybes.or(state.must());
            mergedMaybes.or(state.maybe());
            mergedDepth = max(mergedDepth, state.depth());

            for (int i = state.must().nextSetBit(0); i >= 0; i = state.must().nextSetBit(i + 1)) {
                mergedCut[i] = min(mergedCut[i], state.cut()[i]);
            }

            for (int i = state.maybe().nextSetBit(0); i >= 0; i = state.maybe().nextSetBit(i + 1)) {
                mergedCut[i] = min(mergedCut[i], state.cut()[i]);
            }
        }

        mergedMaybes.andNot(mergedMust);

        return new SRFLPState(mergedMust, mergedMaybes, mergedCut, mergedDepth);
    }
    /**
     * Relaxation of an edge cost between two states.
     *
     * <p>
     * In this implementation, the edge cost is not modified by the relaxation and
     * is returned as-is.
     * </p>
     *
     * @param from   The source state.
     * @param to     The target state.
     * @param merged The merged state containing this edge.
     * @param d      The decision taken to move from {@code from} to {@code to}.
     * @param cost   The original cost of the edge.
     * @return The relaxed cost of the edge, which in this case is equal to {@code cost}.
     */
    @Override
    public double relaxEdge(SRFLPState from, SRFLPState to, SRFLPState merged, Decision d,
                            double cost) {
        return cost;
    }
}
