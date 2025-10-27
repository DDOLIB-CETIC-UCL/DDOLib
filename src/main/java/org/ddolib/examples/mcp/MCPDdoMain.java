package org.ddolib.examples.mcp;

import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.modeling.DdoModel;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Relaxation;
import org.ddolib.modeling.Solvers;
import org.ddolib.util.io.SolutionPrinter;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
/**
 * Main class for solving the <b>Maximum Cut Problem (MCP)</b> using a DDO (Decision Diagram Optimization) approach.
 * <p>
 * This class demonstrates how to set up a DDO model for the MCP, execute the search, and print
 * the resulting solutions and statistics.
 * </p>
 *
 * <p>
 * The problem instance can be provided as a command-line argument. If no argument is provided,
 * a default instance located at <code>data/MCP/mcp_5_2.txt</code> is used.
 * </p>
 *
 * <p>
 * The model uses {@link MCPRelax} for state relaxation, {@link MCPRanking} for state ranking,
 * and {@link MCPFastLowerBound} for computing fast lower bounds. The {@link DdoModel} framework
 * handles the search process using DDO.
 * </p>
 *
 * @see MCPProblem
 * @see MCPState
 * @see DdoModel
 * @see MCPRelax
 * @see MCPRanking
 * @see MCPFastLowerBound
 * @see Solvers
 */
public final class MCPDdoMain {
    /**
     * Entry point of the application.
     * <p>
     * Initializes the MCP problem, builds a DDO model, and runs the search to find
     * a maximum cut. The resulting solutions are printed incrementally via
     * {@link SolutionPrinter} and a final {@link SearchStatistics} summary is displayed.
     * </p>
     *
     * @param args optional command-line arguments; args[0] can specify the path to an MCP instance file
     * @throws IOException if the instance file cannot be read
     */

    public static void main(String[] args) throws IOException {
        final String instance = args.length == 0 ? Path.of("data", "MCP", "mcp_5_2.txt").toString() : args[0];
        final MCPProblem problem = new MCPProblem(instance);
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

        SearchStatistics stats = Solvers.minimizeDdo(model, (sol, s) -> {
            SolutionPrinter.printSolution(s,sol);
        });
        System.out.println(stats);

    }
}
