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
    private int[] estimation;
    private int[] partialSum;

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
                    merged.set(i, min(mergedI, currentI));
                } else if (signum(mergedI) == -1 && signum(currentI) == -1) {
                    merged.set(i, max(mergedI, currentI));
                } else {
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

    private int[] precomputeAllEstimate() {
        int[] toReturn = new int[problem.nbVars()];
        for (int node = 0; node < problem.nbVars(); node++) {
            toReturn[node] = precomputeEstimate(node);
        }
        return toReturn;
    }

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

    private int[] precomputeAllPartialSum() {
        int[] toReturn = new int[problem.nbVars()];
        for (int node = 0; node < problem.nbVars(); node++) {
            toReturn[node] = precomputePartialSum(node);
        }
        return toReturn;
    }
}
