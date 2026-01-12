package org.ddolib.examples.mcp;

import org.ddolib.ddo.core.heuristics.cluster.StateCoordinates;

public class MCPCoordinates implements StateCoordinates<MCPState> {
    @Override
    public double[] getCoordinates(MCPState state) {
        double[] coordinates = new double[state.netBenefit().size()];
        for (int i = 0; i < coordinates.length; i++) {
            coordinates[i] = state.netBenefit().get(i);
        }

        return coordinates;
    }
}
