package org.ddolib.examples.layered.mcp;

import java.io.IOException;
import java.nio.file.Path;
import org.ddolib.common.util.io.SolutionPrinter;
import org.ddolib.layered.modeling.LnsModel;
import org.ddolib.layered.modeling.Problem;
import org.ddolib.layered.modeling.Solvers;
import org.ddolib.layered.solver.Solution;

/**
 * Entry point for solving the Maximum Cut Problem (MCP) using a Large Neighborhood Search (LNS)
 * approach combined with Decision Diagram Optimization (DDO).
 *
 * <p>The Maximum Cut problem consists in partitioning the vertices of a graph into two disjoint
 * sets such that the sum of the weights of edges crossing the partition (i.e., edges with endpoints
 * in different sets) is maximized.
 *
 * <p>This class demonstrates how to:
 *
 * <ul>
 *   <li>Load a Maximum Cut instance from a file
 *   <li>Define a {@code LnsModel} with problem-specific components
 *   <li>Use a lower bound heuristic to guide the search
 *   <li>Run a Large Neighborhood Search (LNS) optimization
 *   <li>Print intermediate and final solutions
 * </ul>
 *
 * <h2>Execution</h2>
 *
 * <p>The program accepts an optional command-line argument specifying the path to a Maximum Cut
 * instance file. If not provided, a default instance is loaded from:
 *
 * <pre>
 * data/MCP/mcp_5_2.txt
 * </pre>
 *
 * <h2>Model Components</h2>
 *
 * <ul>
 *   <li>{@link MCPProblem} – defines the graph and edge weights
 *   <li>{@link MCPFastLowerBound} – provides a fast lower bound on the cut value
 *   <li>{@link MCPRanking} – ranks states during decision diagram compilation
 * </ul>
 *
 * <h2>Search Configuration</h2>
 *
 * <ul>
 *   <li>Search strategy: Large Neighborhood Search (LNS)
 *   <li>Time limit: 1000 milliseconds
 * </ul>
 *
 * <p>No dominance rule or width heuristic is explicitly specified, so default behaviors (if
 * provided by the framework) are used.
 *
 * <h2>Output</h2>
 *
 * <p>The program prints:
 *
 * <ul>
 *   <li>Intermediate solutions during the search
 *   <li>Final solution statistics
 *   <li>The best partition (cut) found
 * </ul>
 *
 * @see MCPProblem
 * @see MCPState
 * @see MCPFastLowerBound
 * @see MCPRanking
 * @see LnsModel
 * @see Solvers#minimizeLns
 * @see Solution
 */
public class MCPLnsMain {

    private MCPLnsMain() {}

    /**
     * Main entry point of the program.
     *
     * <p>Loads a Maximum Cut instance, configures the LNS model with problem-specific heuristics,
     * and runs the optimization process.
     *
     * @param args optional command-line arguments:
     *     <ul>
     *       <li>{@code args[0]} – path to the MCP instance file
     *     </ul>
     *
     * @throws IOException if the instance file cannot be read
     */
    public static void main(String[] args) throws IOException {

        final String instance =
                args.length == 0 ? Path.of("data", "MCP", "mcp_5_2.txt").toString() : args[0];

        final MCPProblem problem = new MCPProblem(instance);

        LnsModel<MCPState> model =
                new LnsModel<MCPState>() {
                    @Override
                    public Problem<MCPState> problem() {
                        return problem;
                    }

                    @Override
                    public MCPFastLowerBound lowerBound() {
                        return new MCPFastLowerBound(problem);
                    }

                    @Override
                    public MCPRanking ranking() {
                        return new MCPRanking();
                    }
                };

        Solution bestSolution =
                Solvers.minimizeLns(
                        model,
                        s -> s.runtime() > 1000,
                        (sol, s) -> {
                            SolutionPrinter.printSolution(s, sol);
                        });

        System.out.println(bestSolution.statistics());
        System.out.println(bestSolution);
    }
}
