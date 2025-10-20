package org.ddolib.examples.mcp;

import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.modeling.DdoModel;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Relaxation;
import org.ddolib.modeling.Solvers;

import java.io.IOException;
import java.nio.file.Paths;

public final class MCPDdoMain {
    /**
     * ******* Maximum Cut Problem (MCP) *******
     *
     * @param args
     * @throws IOException
     */

    public static void main(String[] args) throws IOException {

        final String filename = Paths.get("data", "MCP", "mcp_5_2.txt").toString();
        final MCPProblem problem = new MCPProblem(filename);
        DdoModel<MCPState> model = new DdoModel<MCPState>() {
            @Override
            public Problem<MCPState> problem() {
                return problem;
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

        Solvers<MCPState> solver = new Solvers<>();

        SearchStatistics stats = solver.minimizeDdo(model);

        System.out.println(stats);

    }
}
