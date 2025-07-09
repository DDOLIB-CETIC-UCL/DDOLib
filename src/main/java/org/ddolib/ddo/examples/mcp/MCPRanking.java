package org.ddolib.ddo.examples.mcp;

import org.ddolib.ddo.modeling.StateRanking;

/**
 * Class used to compare two states for the MCP problem.
 * <br>
 * When comparing two states, the best is the one that can generate the biggest benefit, independently of the decisions.
 * That's why we sum the absolute value of each benefit to compare the states.
 */
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
