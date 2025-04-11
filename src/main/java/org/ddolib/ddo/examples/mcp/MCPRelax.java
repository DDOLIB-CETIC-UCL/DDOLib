package org.ddolib.ddo.examples.mcp;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Relaxation;

import java.util.ArrayList;
import java.util.Iterator;

import static java.lang.Integer.*;
import static java.lang.Math.abs;


public class MCPRelax implements Relaxation<MCPState> {

    final MCPProblem problem;

    public MCPRelax(MCPProblem problem) {
        this.problem = problem;
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
}
