package org.ddolib.ddo.examples.mcp;

import org.ddolib.ddo.heuristics.StateRanking;

public class MCPRanking implements StateRanking<MCPState> {

    public static int rank(MCPState state) {
        int toReturn = 0;
        for (int i = state.depth(); i < state.netBenefit().size(); i++) {
            toReturn += Math.abs(state.netBenefit().get(i));

        }
        return toReturn;
    }

    @Override
    public int compare(MCPState o1, MCPState o2) {
        return Integer.compare(rank(o1), rank(o2));
    }


}
