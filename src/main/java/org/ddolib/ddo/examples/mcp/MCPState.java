package org.ddolib.ddo.examples.mcp;

import java.util.ArrayList;

/**
 * @param netBenefit Net Benefit to put node i in set T
 * @param depth
 */
public record MCPState(ArrayList<Integer> netBenefit, int depth) {

    @Override
    public String toString() {
        return netBenefit.toString();
    }
}
