package org.ddolib.examples.misp;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.Relaxation;

import java.util.BitSet;
import java.util.Iterator;
/**
 * Implements a relaxation strategy for the Maximum Independent Set Problem (MISP)
 * to be used in decision diagram optimization (DDO) algorithms.
 * <p>
 * This relaxation defines how to merge multiple states and how to adjust transition
 * costs when exploring the relaxed search space.
 * </p>
 * <p>
 * In this implementation:
 * </p>
 * <ul>
 *     <li>The merged state is computed as the union of all given states, meaning all nodes
 *         that are available in at least one state are considered available in the merged state.</li>
 *     <li>The edge relaxation does not modify the transition cost; it returns the original cost.</li>
 * </ul>
 */
public class MispRelax implements Relaxation<BitSet> {

    /**
     * The MISP problem instance.
     */
    private final MispProblem problem;

    /**
     * Constructs a relaxation for the given MISP problem instance.
     *
     * @param problem the Maximum Independent Set problem instance
     */

    public MispRelax(MispProblem problem) {
        this.problem = problem;
    }
    /**
     * Merges multiple states into a single relaxed state.
     * <p>
     * The merged state is the union of all input states: a node is considered available
     * if it is available in at least one of the states.
     * </p>
     *
     * @param states an iterator over the states to merge
     * @return the merged state representing an over-approximation of all input states
     */
    @Override
    public BitSet mergeStates(Iterator<BitSet> states) {
        var merged = new BitSet(problem.nbVars());
        while (states.hasNext()) {
            final BitSet state = states.next();
            // the merged state is the union of all the state
            merged.or(state);
        }
        return merged;
    }
    /**
     * Adjusts the transition cost when moving from one state to another in the relaxed space.
     * <p>
     * In this implementation, the relaxation does not modify the cost and simply returns it.
     * </p>
     *
     * @param from the source state
     * @param to the destination state
     * @param merged the merged state
     * @param d the decision applied
     * @param cost the original transition cost
     * @return the relaxed transition cost (here equal to {@code cost})
     */
    @Override
    public double relaxEdge(BitSet from, BitSet to, BitSet merged, Decision d, double cost) {
        return cost;
    }
}