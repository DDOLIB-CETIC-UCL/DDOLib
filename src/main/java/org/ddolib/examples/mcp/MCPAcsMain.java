package org.ddolib.examples.mcp;

import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.modeling.AcsModel;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solvers;

import java.io.IOException;
import java.nio.file.Paths;

public final class MCPAcsMain {
    /**
     * ******* Maximum Cut Problem (MCP) *******
     *
     * @param args
     * @throws IOException
     */

    public static void main(String[] args) throws IOException {

        final String filename = Paths.get("data", "MCP", "mcp_5_2.txt").toString();
        final MCPProblem problem = new MCPProblem(filename);
        AcsModel<MCPState> model = new AcsModel<MCPState>() {
            @Override
            public Problem<MCPState> problem() {
                return problem;
            }

            @Override
            public MCPFastLowerBound lowerBound() {
                return new MCPFastLowerBound(problem);
            }
        };

        SearchStatistics stats = Solvers.minimizeAcs(model);

        System.out.println(stats);

    }
}
