package org.ddolib.examples.talentscheduling;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.Relaxation;

import java.util.BitSet;
import java.util.Iterator;
/**
 * Implementation of a relaxation for the Talent Scheduling problem (TSP).
 *
 * <p>
 * This class defines how multiple {@link TSState} instances can be merged into a single
 * relaxed state and how edge costs are relaxed in the context of DDO or other
 * relaxed decision diagrams.
 * </p>
 *
 * <p>
 * The relaxation is implemented by merging the remaining and maybe scenes from multiple states:
 * </p>
 * <ul>
 *     <li>{@code mergedRemaining} is the intersection of all {@code remainingScenes} sets.</li>
 *     <li>{@code mergedMaybe} is the union of all {@code remainingScenes} and {@code maybeScenes},
 *         minus the {@code mergedRemaining} scenes.</li>
 * </ul>
 */
public class TSRelax implements Relaxation<TSState> {

    private final TSProblem problem;
    /**
     * Constructs a new relaxation instance for the given Talent Scheduling problem.
     *
     * @param problem The TSP problem instance associated with this relaxation.
     */
    public TSRelax(TSProblem problem) {
        this.problem = problem;
    }
    /**
     * Merges multiple {@link TSState} instances into a single relaxed state.
     *
     * @param states An iterator over the states to merge.
     * @return A new {@link TSState} representing the merged state.
     */
    @Override
    public TSState mergeStates(Iterator<TSState> states) {
        BitSet mergedRemaining = new BitSet(problem.nbVars());
        mergedRemaining.set(0, problem.nbVars(), true);
        BitSet mergedMaybe = new BitSet(problem.nbVars());

        while (states.hasNext()) {
            TSState state = states.next();
            mergedRemaining.and(state.remainingScenes());
            mergedMaybe.or(state.remainingScenes());
            mergedMaybe.or(state.maybeScenes());
        }
        mergedMaybe.andNot(mergedRemaining);

        return new TSState(mergedRemaining, mergedMaybe);
    }
    /**
     * Returns the relaxed edge cost between two states.
     *
     * <p>In this implementation, the cost is not changed and returned as-is.</p>
     *
     * @param from   The source state.
     * @param to     The target state.
     * @param merged The merged state if multiple states are combined.
     * @param d      The decision associated with the edge.
     * @param cost   The original edge cost.
     * @return The relaxed edge cost (here equal to {@code cost}).
     */
    @Override
    public double relaxEdge(TSState from, TSState to, TSState merged, Decision d, double cost) {
        return cost;
    }

}
