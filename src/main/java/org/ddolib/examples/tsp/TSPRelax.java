package org.ddolib.examples.tsp;


import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.Relaxation;

import java.util.BitSet;
import java.util.Iterator;
/**
 * Implementation of a relaxation for the Traveling Salesman Problem (TSP).
 *
 * <p>
 * This class implements the {@link Relaxation} interface for {@link TSPState}.
 * It provides methods to merge multiple states into a single relaxed state
 * and to adjust the cost of transitions in a relaxed context.
 * </p>
 *
 * <p>
 * In this implementation:
 * </p>
 * <ul>
 *     <li>{@link #mergeStates(Iterator)} returns a state where the "toVisit" and "current"
 *         sets are the union of the corresponding sets from the input states.</li>
 *     <li>{@link #relaxEdge(TSPState, TSPState, TSPState, Decision, double)} currently
 *         returns the original cost without modification.</li>
 * </ul>
 *
 * @see TSPState
 * @see Relaxation
 */
public class TSPRelax implements Relaxation<TSPState> {

    private final TSPProblem problem;
    /**
     * Constructs a relaxation for a given TSP problem.
     *
     * @param problem the TSP problem instance to be relaxed
     */
    public TSPRelax(TSPProblem problem) {
        this.problem = problem;
    }
    /**
     * Merges multiple {@link TSPState} instances into a single relaxed state.
     *
     * <p>
     * The resulting state has its "toVisit" and "current" sets as the union of the corresponding
     * sets from all input states.
     * </p>
     *
     * @param states an iterator over the states to merge
     * @return a new {@link TSPState} representing the merged state
     */
    @Override
    public TSPState mergeStates(final Iterator<TSPState> states) {
        BitSet toVisit = new BitSet(problem.n);
        BitSet current = new BitSet(problem.n);

        while (states.hasNext()) {
            TSPState state = states.next();
            toVisit.or(state.toVisit); // union
            current.or(state.current); // union
        }

        return new TSPState(current, toVisit);
    }
    /**
     * Relaxes the cost of a transition between states.
     *
     * <p>
     * In this implementation, the cost is not modified and the original cost is returned.
     * </p>
     *
     * @param from   the state before the transition
     * @param to     the state after the transition
     * @param merged the merged state (if any)
     * @param d      the decision causing the transition
     * @param cost   the original cost of the transition
     * @return the relaxed cost of the transition (same as the original cost)
     */
    @Override
    public double relaxEdge(TSPState from, TSPState to, TSPState merged, Decision d, double cost) {
        return cost;
    }

}

