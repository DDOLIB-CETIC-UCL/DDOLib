package org.ddolib.examples.mcp;

import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.modeling.AcsModel;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solvers;
import org.ddolib.util.io.SolutionPrinter;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
/**
 * Maximum Cut Problem (MCP) with Acs.
 * Main class for solving the <b>Maximum Cut Problem (MCP)</b> using an Anytime Column Search (ACS) approach.
 * <p>
 * This class demonstrates how to set up an ACS model for the MCP, run the search, and print
 * the resulting solution and statistics.
 * </p>
 *
 * <p>
 * The problem instance can be provided as a command-line argument. If no argument is provided,
 * a default instance located at <code>data/MCP/mcp_5_2.txt</code> is used.
 * </p>
 *
 * <p>
 * The model uses {@link MCPFastLowerBound} to compute a fast lower bound of the solution quality,
 * and the {@link AcsModel} framework handles the search process.
 * </p>
 *
 * @see MCPProblem
 * @see MCPState
 * @see AcsModel
 * @see MCPFastLowerBound
 * @see Solvers
 */
public final class MCPAcsMain {
    /**
     * Entry point of the application.
     * <p>
     * Initializes the MCP problem, builds an ACS model, and runs the ACS search to find
     * a maximum cut. The resulting solution is printed incrementally via
     * {@link SolutionPrinter} and a final {@link SearchStatistics} summary is displayed.
     * </p>
     *
     * @param args optional command-line arguments; args[0] can specify the path to an MCP instance file
     * @throws IOException if the instance file cannot be read
     */

    public static void main(String[] args) throws IOException {
        final String instance = args.length == 0 ? Path.of("data", "MCP", "mcp_5_2.txt").toString() : args[0];
        final MCPProblem problem = new MCPProblem(instance);
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

        SearchStatistics stats = Solvers.minimizeAcs(model, (sol, s) -> {
            SolutionPrinter.printSolution(s,sol);
        });

        System.out.println(stats);

    }
}
