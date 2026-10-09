package org.ddolib.examples.layered.srflp;

import java.io.IOException;
import java.nio.file.Paths;
import org.ddolib.common.heuristics.width.FixedWidth;
import org.ddolib.common.heuristics.width.WidthHeuristic;
import org.ddolib.common.util.io.SolutionPrinter;
import org.ddolib.layered.modeling.FastLowerBound;
import org.ddolib.layered.modeling.LnsModel;
import org.ddolib.layered.modeling.Problem;
import org.ddolib.layered.modeling.Solvers;
import org.ddolib.layered.modeling.StateRanking;
import org.ddolib.layered.solver.Solution;

/**
 * Entry point for solving the Single Row Facility Layout Problem (SRFLP) using a Large Neighborhood
 * Search (LNS) approach.
 *
 * <p>This class reads an instance file describing an SRFLP problem, constructs an LNS model, and
 * attempts to find an optimal or near-optimal solution within a time limit. The solution and its
 * statistics are printed to standard output.
 *
 * <p>Usage:
 *
 * <pre>
 * java SRFLPLnsMain [instanceFilePath] [maxWidth]
 * </pre>
 *
 * <ul>
 *   <li>{@code instanceFilePath} (optional): Path to the SRFLP instance file. If omitted, the
 *       default instance located at {@code data/SRFLP/simple} is used.
 *   <li>{@code maxWidth} (optional): Maximum width for the search tree heuristic. Defaults to 50 if
 *       not provided.
 * </ul>
 *
 * <p>The LNS model is configured with:
 *
 * <ul>
 *   <li>A {@link SRFLPFastLowerBound} for fast estimation of lower bounds.
 *   <li>A {@link SRFLPRanking} to rank decisions during the search.
 *   <li>A fixed width heuristic ({@link FixedWidth}) with the specified {@code maxWidth}.
 * </ul>
 *
 * <p>The search is time-limited (100 milliseconds) per iteration, and the best solution found is
 * printed along with its statistics.
 *
 * <p>This class does not currently implement dominance checks.
 *
 * @version 1.0
 */
public class SRFLPLnsMain {

    private SRFLPLnsMain() {}

    /**
     * Main method to run the SRFLP LNS solver.
     *
     * @param args optional command-line arguments:
     *     <ol>
     *       <li>{@code args[0]}: path to the SRFLP instance file (default: {@code
     *           data/SRFLP/simple})
     *       <li>{@code args[1]}: maximum width for the width heuristic (default: 50)
     *     </ol>
     *
     * @throws IOException if there is an error reading the instance file
     */
    public static void main(String[] args) throws IOException {
        final String filename =
                args.length == 0 ? Paths.get("data", "SRFLP", "simple").toString() : args[0];
        final int maxWidth = args.length > 1 ? Integer.parseInt(args[1]) : 50;

        final SRFLPProblem problem = new SRFLPProblem(filename);

        LnsModel<SRFLPState> model =
                new LnsModel<>() {
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

        Solution bestSolution =
                Solvers.minimizeLns(
                        model,
                        s -> s.runtime() > 1000,
                        (sol, stat) -> {
                            SolutionPrinter.printSolution(stat, sol);
                        });

        System.out.println("\n");
        System.out.println("===== Optimal Solution =====");
        System.out.println(bestSolution.statistics());
        System.out.println(bestSolution);
    }
}
