package org.ddolib.examples.mcp;

import org.ddolib.ddo.core.heuristics.cluster.StateDistance;
import static org.ddolib.util.DistanceUtil.euclideanDistance;

public class MCPDistance implements StateDistance<MCPState> {
    @Override
    public double distance(MCPState a, MCPState b) {
        return euclideanDistance(a.netBenefit(), b.netBenefit());
    }
}
