package org.ddolib.examples.layered.tsp;

import java.io.IOException;
import java.nio.file.Paths;
import org.ddolib.common.heuristics.width.FixedWidth;
import org.ddolib.common.heuristics.width.WidthHeuristic;
import org.ddolib.common.util.io.SolutionPrinter;
import org.ddolib.layered.modeling.LnsModel;
import org.ddolib.layered.modeling.Problem;
import org.ddolib.layered.modeling.Solvers;
import org.ddolib.layered.solver.Solution;

/**
 * Entry point for solving the Traveling Salesman Problem (TSP) using a Large Neighborhood Search
 * (LNS) approach.
 *
 * <p>This class reads an instance file describing a TSP problem, constructs an LNS model, and
 * searches for an optimal or near-optimal tour within a time limit. The best solution found is
 * printed to standard output.
 *
 * <p>Usage:
 *
 * <pre>
 * java TSPLnsMain [instanceFilePath]
 * </pre>
 *
 * <ul>
 *   <li>{@code instanceFilePath} (optional): Path to the TSP instance file. If omitted, the default
 *       instance located at {@code data/TSP/instance_18_0.xml} is used.
 * </ul>
 *
 * <p>The LNS model is configured with:
 *
 * <ul>
 *   <li>A {@link TSPFastLowerBound} for efficient lower bound estimation.
 *   <li>A {@link TSPRanking} to rank tour decisions during the search.
 *   <li>A fixed width heuristic ({@link FixedWidth}) with a width of 500 for tree exploration.
 * </ul>
 *
 * <p>The search is limited to 10,000 milliseconds (10 seconds) per iteration, and the best solution
 * found is printed using {@link SolutionPrinter}.
 *
 * <p>This implementation does not include dominance checks.
 *
 * @version 1.0
 */
public class TSPLnsMain {

    private TSPLnsMain() {}

    /**
     * Main method to run the TSP LNS solver.
     *
     * @param args optional command-line argument:
     *     <ul>
     *       <li>{@code args[0]}: path to the TSP instance file (default: {@code
     *           data/TSP/instance_18_0.xml})
     *     </ul>
     *
     * @throws IOException if there is an error reading the instance file
     */
    public static void main(final String[] args) throws IOException {
        String instance =
                args.length == 0
                        ? Paths.get("data", "TSP", "instance_18_0.xml").toString()
                        : args[0];
        final TSPProblem problem = new TSPProblem(instance);
        LnsModel<TSPState> model =
                new LnsModel<TSPState>() {
                    @Override
                    public Problem<TSPState> problem() {
                        return problem;
                    }

                    @Override
                    public TSPFastLowerBound lowerBound() {
                        return new TSPFastLowerBound(problem);
                    }

                    @Override
                    public TSPRanking ranking() {
                        return new TSPRanking();
                    }

                    @Override
                    public WidthHeuristic<TSPState> widthHeuristic() {
                        return new FixedWidth<>(500);
                    }
                };

        Solution bestSolution =
                Solvers.minimizeLns(
                        model,
                        s -> s.runtime() > 10000,
                        (sol, s) -> {
                            SolutionPrinter.printSolution(s, sol);
                        });
        System.out.println(bestSolution);
    }
}
