package org.ddolib.examples.ddo.mcp;

import org.ddolib.modeling.FastUpperBound;

import java.util.Set;

/**
 * Implementation of fast upper bound heuristic for the MCP.
 */
public class MCPFastUpperBound implements FastUpperBound<MCPState> {

    final MCPProblem problem;
    private final double initVal;
    private final int[] estimation;
    private final int[] partialSum;

    public MCPFastUpperBound(MCPProblem problem) {
        this.problem = problem;
        initVal = problem.initialValue();
        estimation = precomputeAllEstimate();
        partialSum = precomputeAllPartialSum();
    }

    @Override
    public double fastUpperBound(MCPState state, Set<Integer> variables, double lb) {
        int k = state.depth();
        if (k == problem.nbVars()) return 0.0;
        else return MCPRanking.rank(state) + estimation[k] + partialSum[k] - initVal;
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
