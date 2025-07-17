package org.ddolib.ddo.examples.mcp;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.Relaxation;

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
            for (int i = depth; i < problem.nbVars(); i++) {
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
    public double relaxEdge(MCPState from, MCPState to, MCPState merged, Decision d, double cost) {
        double toReturn = cost;
        for (int i = d.var() + 1; i < problem.nbVars(); i++) {
            toReturn += abs(to.netBenefit().get(i)) - abs(merged.netBenefit().get(i));
        }
        return toReturn;
    }
}
