package org.ddolib.ddo.examples.mcp;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Relaxation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import static java.lang.Integer.*;
import static java.lang.Math.abs;


public class MCPRelax implements Relaxation<MCPState> {

    final MCPProblem problem;
    private final int initVal;
    private final int[] estimation;
    private final int[] partialSum;

    public MCPRelax(MCPProblem problem) {
        this.problem = problem;
        initVal = problem.initialValue();
        estimation = precomputeAllEstimate();
        partialSum = precomputeAllPartialSum();
    }

    @Override
    public MCPState mergeStates(Iterator<MCPState> states) {
        MCPState state = states.next();
        ArrayList<Integer> merged = new ArrayList<>(state.netBenefit());
        int depth = state.depth();

        while (states.hasNext()) {
            MCPState current = states.next();
            for (int i = 0; i < problem.nbVars(); i++) {
                Integer mergedI = merged.get(i);
                Integer currentI = current.netBenefit().get(i);


                if (signum(mergedI) == 1 && signum(currentI) == 1) {
                    //If all the net benefits are positive, we keep the smallest one
                    merged.set(i, min(mergedI, currentI));
                } else if (signum(mergedI) == -1 && signum(currentI) == -1) {
                    // If all the net benefits are negative, we keep the biggest one
                    merged.set(i, max(mergedI, currentI));
                } else {
                    // Otherwise, we set at 0
                    merged.set(i, 0);
                }
            }
        }
        return new MCPState(merged, depth);
    }

    @Override
    public int relaxEdge(MCPState from, MCPState to, MCPState merged, Decision d, int cost) {
        int toReturn = cost;
        for (int i = d.var() + 1; i < problem.nbVars(); i++) {
            toReturn += abs(to.netBenefit().get(i)) - abs(merged.netBenefit().get(i));
        }
        return toReturn;
    }

    @Override
    public int fastUpperBound(MCPState state, Set<Integer> variables) {
        int k = state.depth();
        return MCPRanking.rank(state) + estimation[k] + partialSum[k] - initVal;
    }


    /* Some part of the upper bound can be precomputed (some partial sum).
    These methods are called at initialization.
    */

    /**
     * Returns the sum of positive weight from edges starting at node bigger than the given depth.
     * It approximates the solution of the given state.
     */
    private int precomputeEstimate(int depth) {
        int toReturn = 0;
        for (int from = depth; from < problem.nbVars(); from++) {
            for (int to = from + 1; to < problem.nbVars(); to++) {
                int w = problem.graph.weightOf(from, to);
                if (w > 0) toReturn += w;
            }
        }
        return toReturn;
    }

    /**
     * Returns the sum of positive weight for all the depth
     */
    private int[] precomputeAllEstimate() {
        int[] toReturn = new int[problem.nbVars()];
        for (int node = 0; node < problem.nbVars(); node++) {
            toReturn[node] = precomputeEstimate(node);
        }
        return toReturn;
    }

    /**
     * Returns the partial sum of negative weight of edges ending at nodes smaller than the given edges.
     */
    private int precomputePartialSum(int depth) {
        int toReturn = 0;
        for (int j = 0; j < depth; j++) {
            for (int i = 0; i < j; i++) {
                int w = problem.graph.weightOf(i, j);
                if (w < 0) toReturn += w;
            }
        }
        return toReturn;
    }

    /**
     * Returns the partial sum for all depths
     */
    private int[] precomputeAllPartialSum() {
        int[] toReturn = new int[problem.nbVars()];
        for (int node = 0; node < problem.nbVars(); node++) {
            toReturn[node] = precomputePartialSum(node);
        }
        return toReturn;
    }
}
