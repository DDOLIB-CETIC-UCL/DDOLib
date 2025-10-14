package org.ddolib.examples.mcp;

import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.modeling.Model;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solvers;

import java.io.IOException;
import java.nio.file.Paths;

public final class MCPAstarMain {
    /**
     * ******* Maximum Cut Problem (MCP) *******
     * Given an undirected weighted graph 𝐺 = (𝑉,𝐸) in which the weight of
     * the edge (𝑖,𝑗) ∈ 𝐸 is denoted 𝑤_{i,j} the MCP consists in finding a bi-partition (𝑆,𝑇)
     * of the vertices of some given graph that maximizes the total weight of edges whose endpoints are in different partitions.
     * This problem is considered in the paper:
     * - David Bergman et al. Decision Diagrams for Optimization. Ed. by Barry O’Sullivan and Michael Wooldridge. Springer, 2016.
     * - David Bergman et al. “Discrete Optimization with Decision Diagrams”. In: INFORMS Journal on Computing 28.1 (2016), pp. 47–66.
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {

        final String filename = Paths.get("data", "MCP", "mcp_5_2.txt").toString();
        Model<MCPState> model = new Model<MCPState>() {
            private MCPProblem problem;

            @Override
            public Problem<MCPState> problem() {
                try {
                    problem = new MCPProblem(filename);
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

        Solvers<MCPState> solver = new Solvers<>();

        SearchStatistics stats = solver.minimizeAstar(model);

        System.out.println(stats);

    }
}
