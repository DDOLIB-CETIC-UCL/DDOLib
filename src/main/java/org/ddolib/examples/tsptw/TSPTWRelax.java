package org.ddolib.examples.tsptw;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.Relaxation;

import java.util.BitSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
/**
 * Relaxation class for the Traveling Salesman Problem with Time Windows (TSPTW).
 * <p>
 * This class implements the {@link Relaxation} interface for {@link TSPTWState}.
 * It provides methods to merge multiple states into a relaxed state and
 * to relax the cost of transitions (edges) between states.
 * </p>
 */
public class TSPTWRelax implements Relaxation<TSPTWState> {

    /** Represents infinity for arrival time. */
    private static final int INFINITY = Integer.MAX_VALUE;

    /** Number of variables/nodes in the TSPTW problem. */
    private final int numVar;

    /**
     * Initializes the relaxation for a given TSPTW problem.
     *
     * @param problem the TSPTW problem to process
     */
    public TSPTWRelax(TSPTWProblem problem) {
        this.numVar = problem.nbVars();
    }
    /**
     * Merges multiple TSPTW states into a single relaxed state.
     * <p>
     * The merge operation consists of:
     * </p>
     * <ul>
     *     <li>Combining visited positions (union of all positions).</li>
     *     <li>Computing the intersection of all "must visit" sets.</li>
     *     <li>Building the "possibly visit" set as the union of all "must visit" and "possibly visit" sets,
     *         then removing the nodes that are mandatory.</li>
     *     <li>Selecting the minimum arrival time among all states.</li>
     *     <li>Keeping the depth of the last processed state.</li>
     * </ul>
     *
     * @param states an iterator over the states to merge
     * @return a new {@link TSPTWState} representing the relaxed state
     */
    @Override
    public TSPTWState mergeStates(Iterator<TSPTWState> states) {
        Set<Integer> mergedPos = new HashSet<>();
        int mergedTime = INFINITY;
        BitSet mergedMust = new BitSet(numVar);
        mergedMust.set(0, numVar, true);
        BitSet mergedPossibly = new BitSet(numVar);
        int mergedDepth = 0;
        while (states.hasNext()) {
            TSPTWState current = states.next();
            //The merged position is the union of all the position
            switch (current.position()) {
                case TSPNode(int value) -> mergedPos.add(value);
                case VirtualNodes(Set<Integer> nodes) -> mergedPos.addAll(nodes);
            }
            // The merged must is the intersection of all must set
            mergedMust.and(current.mustVisit());
            // The merged possibly is the union of the all the must sets and all the possibly sets
            mergedPossibly.or(current.mustVisit());
            mergedPossibly.or(current.possiblyVisit());
            // The arrival time of the merged node is the min of all the arrival times
            mergedTime = Integer.min(mergedTime, current.time());
            mergedDepth = current.depth();
        }
        // We exclude the intersection of the must from the merged possibly
        mergedPossibly.andNot(mergedMust);

        return new TSPTWState(new VirtualNodes(mergedPos), mergedTime, mergedMust, mergedPossibly, mergedDepth);
    }
    /**
     * Relaxes the cost of an edge (transition) between two states.
     * <p>
     * In this implementation, the cost is not modified, and the method simply
     * returns the provided value. This method can be extended to apply more
     * sophisticated relaxations if needed.
     * </p>
     *
     * @param from the source state
     * @param to the target state
     * @param merged the state resulting from the merge or relaxation
     * @param d the decision associated with this transition
     * @param cost the cost of the transition
     * @return the relaxed cost of the transition
     */
    @Override
    public double relaxEdge(TSPTWState from, TSPTWState to, TSPTWState merged, Decision d, double cost) {
        return cost;
    }

}
