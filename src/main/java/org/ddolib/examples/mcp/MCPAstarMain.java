package org.ddolib.examples.mcp;

import org.ddolib.ddo.core.profiling.SearchStatistics;
import org.ddolib.modeling.*;

import java.io.IOException;
import java.nio.file.Paths;

public final class MCPAstarMain {

    public static void main(String[] args) throws IOException {

        final String filename = Paths.get("data", "MCP", "mcp_5_2.txt").toString();
        Model<MCPState> model = new Model<MCPState>() {
            private MCPProblem problem;
            @Override
            public Problem<MCPState> problem() {
                try {
                    problem = MCPIO.readInstance(filename);
                    return problem;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            @Override
            public MCPFastLowerBound lowerBound() {
                return new MCPFastLowerBound(problem);
            }
        };

        Solve<MCPState> solve = new Solve<>();

        SearchStatistics stats = solve.minimize(model);

        solve.onSolution(stats);

    }
}
