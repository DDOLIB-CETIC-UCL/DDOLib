package org.ddolib.examples.mcp;

import org.ddolib.ddo.core.profiling.SearchStatistics;
import org.ddolib.modeling.DdoModel;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Relaxation;
import org.ddolib.modeling.Solve;

import java.io.IOException;
import java.nio.file.Paths;

public final class MCPDdoMain {

    public static void main(String[] args) throws IOException {

        final String filename = Paths.get("data", "MCP", "mcp_5_2.txt").toString();
        DdoModel<MCPState> model = new DdoModel<MCPState>() {
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
            public Relaxation<MCPState> relaxation() {
                return new MCPRelax(problem);
            }

            @Override
            public MCPRanking ranking() {
                return new MCPRanking();
            }
            @Override
            public MCPFastLowerBound lowerBound() {
                return new MCPFastLowerBound(problem);
            }
        };

        Solve<MCPState> solve = new Solve<>();

        SearchStatistics stats = solve.minimizeDdo(model);

        solve.onSolution(stats);

    }
}
