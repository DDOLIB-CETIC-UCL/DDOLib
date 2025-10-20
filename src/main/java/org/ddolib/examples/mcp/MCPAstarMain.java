package org.ddolib.examples.mcp;

import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.modeling.Model;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solvers;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class MCPAstarMain {
    /**
     * ******* Maximum Cut Problem (MCP) *******
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        final String instance = args.length == 0 ? Path.of("data", "MCP", "mcp_5_2.txt").toString() : args[0];
        final MCPProblem problem = new MCPProblem(instance);
        Model<MCPState> model = new Model<MCPState>() {

            @Override
            public Problem<MCPState> problem() {
                return problem;
            }

            @Override
            public MCPFastLowerBound lowerBound() {
                return new MCPFastLowerBound(problem);
            }
        };

        SearchStatistics stats = Solvers.minimizeAstar(model);

        System.out.println(stats);

    }
}
