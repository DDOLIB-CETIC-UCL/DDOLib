package org.ddolib.examples.tsptw;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.common.solver.Solution;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.modeling.LnsModel;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solvers;
import org.ddolib.util.io.SolutionPrinter;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Entry point for solving the Traveling Salesman Problem with Time Windows (TSPTW)
 * using a Large Neighborhood Search (LNS) approach.
 *
 * <p>This class reads an instance file describing a TSPTW problem, constructs an
 * LNS model, and searches for an optimal or near-optimal tour that respects
 * time windows. The best solution found is printed along with runtime statistics.</p>
 *
 * <p>Usage:</p>
 * <pre>
 * java TSPTWLnsMain [instanceFilePath]
 * </pre>
 * <ul>
 *     <li>{@code instanceFilePath} (optional): Path to the TSPTW instance file.
 *     If omitted, the default instance located at {@code data/TSPTW/AFG/rbg010a.tw} is used.</li>
 * </ul>
 *
 * <p>The LNS model is configured with:</p>
 * <ul>
 *     <li>A {@link TSPTWRanking} to rank tour decisions during the search.</li>
 *     <li>A {@link TSPTWFastLowerBound} for efficient lower bound estimation.</li>
 *     <li>A {@link SimpleDominanceChecker} with {@link TSPTWDominance} to prune dominated states.</li>
 *     <li>A fixed width heuristic ({@link FixedWidth}) with a width of 10 for tree exploration.</li>
 * </ul>
 *
 * <p>The search is limited to 1000 milliseconds per iteration, and the best
 * solution found is printed to {@link System#out} along with statistics.</p>
 *
 * @author
 * @version 1.0
 */
public class TSPTWLnsMain {
    /**
     * Main method to run the TSPTW LNS solver.
     *
     * @param args optional command-line argument:
     *             <ul>
     *                 <li>{@code args[0]}: path to the TSPTW instance file
     *                 (default: {@code data/TSPTW/AFG/rbg010a.tw})</li>
     *             </ul>
     * @throws IOException if there is an error reading the instance file.
     */
    public static void main(String[] args) throws IOException {
        final String instance = args.length == 0 ? Path.of("data", "TSPTW", "AFG", "rbg010a.tw").toString() : args[0];
        final TSPTWProblem problem = new TSPTWProblem(instance);
        LnsModel<TSPTWState> model = new LnsModel<>() {
            @Override
            public Problem<TSPTWState> problem() {
                return problem;
            }

            @Override
            public TSPTWRanking ranking() {
                return new TSPTWRanking();
            }

            @Override
            public TSPTWFastLowerBound lowerBound() {
                return new TSPTWFastLowerBound(problem);
            }

            @Override
            public DominanceChecker<TSPTWState> dominance() {
                return new SimpleDominanceChecker<>(new TSPTWDominance(), problem.nbVars());
            }

            @Override
            public WidthHeuristic<TSPTWState> widthHeuristic() {
                return new FixedWidth<>(10);
            }

        };

        Solution bestSolution = Solvers.minimizeLns(model, s -> s.runTimeMs() < 1000, (sol, s) -> {
            SolutionPrinter.printSolution(s, sol);
        });

        System.out.println(bestSolution.statistics());
        System.out.println(bestSolution);
    }
}