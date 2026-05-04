package org.ddolib.examples.srflp;

import org.ddolib.common.solver.Solution;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.modeling.*;
import org.ddolib.util.io.SolutionPrinter;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * Entry point for solving the Single Row Facility Layout Problem (SRFLP)
 * using a Large Neighborhood Search (LNS) approach.
 *
 * <p>This class reads an instance file describing an SRFLP problem, constructs an
 * LNS model, and attempts to find an optimal or near-optimal solution within a
 * time limit. The solution and its statistics are printed to standard output.</p>
 *
 * <p>Usage:</p>
 * <pre>
 * java SRFLPLnsMain [instanceFilePath] [maxWidth]
 * </pre>
 * <ul>
 *     <li>{@code instanceFilePath} (optional): Path to the SRFLP instance file.
 *     If omitted, the default instance located at {@code data/SRFLP/simple} is used.</li>
 *     <li>{@code maxWidth} (optional): Maximum width for the search tree heuristic.
 *     Defaults to 50 if not provided.</li>
 * </ul>
 *
 * <p>The LNS model is configured with:</p>
 * <ul>
 *     <li>A {@link SRFLPFastLowerBound} for fast estimation of lower bounds.</li>
 *     <li>A {@link SRFLPRanking} to rank decisions during the search.</li>
 *     <li>A fixed width heuristic ({@link FixedWidth}) with the specified {@code maxWidth}.</li>
 * </ul>
 *
 * <p>The search is time-limited (100 milliseconds) per iteration, and the best
 * solution found is printed along with its statistics.</p>
 *
 * <p>This class does not currently implement dominance checks.</p>
 *
 * @author
 * @version 1.0
 */
public class SRFLPLnsMain {
    /**
     * Main method to run the SRFLP LNS solver.
     *
     * @param args optional command-line arguments:
     *             <ol>
     *                 <li>{@code args[0]}: path to the SRFLP instance file (default: {@code data/SRFLP/simple})</li>
     *                 <li>{@code args[1]}: maximum width for the width heuristic (default: 50)</li>
     *             </ol>
     * @throws IOException if there is an error reading the instance file.
     */
    public static void main(String[] args) throws IOException {
        final String filename = args.length == 0 ? Paths.get("data", "SRFLP", "simple").toString() :
                args[0];
        final int maxWidth = args.length > 1 ? Integer.parseInt(args[1]) : 50;

        final SRFLPProblem problem = new SRFLPProblem(filename);

        LnsModel<SRFLPState> model = new LnsModel<>() {
            @Override
            public Problem<SRFLPState> problem() {
                return problem;
            }

            @Override
            public FastLowerBound<SRFLPState> lowerBound() {
                return new SRFLPFastLowerBound(problem);
            }

            @Override
            public StateRanking<SRFLPState> ranking() {
                return new SRFLPRanking();
            }

            @Override
            public WidthHeuristic<SRFLPState> widthHeuristic() {
                return new FixedWidth<>(maxWidth);
            }
        };

        Solution bestSolution = Solvers.minimizeLns(model, s -> s.runtime() < 100, (sol, stat) -> {
            SolutionPrinter.printSolution(stat, sol);
        });

        System.out.println("\n");
        System.out.println("===== Optimal Solution =====");
        System.out.println(bestSolution.statistics());
        System.out.println(bestSolution);
    }
}