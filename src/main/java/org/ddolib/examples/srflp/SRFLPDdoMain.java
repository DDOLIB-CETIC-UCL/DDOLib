package org.ddolib.examples.srflp;

import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.modeling.*;
import org.ddolib.util.io.SolutionPrinter;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * The Single-Row Facility Layout Problem (SRFLP) with Ddo.
 * Entry point for solving the Single-Row Facility Layout Problem (SRFLP)
 * using the Decision Diagram Optimization (DDO) algorithm.
 * <p>
 * <strong>Usage:</strong>
 * </p>
 * <pre>{@code
 * java SRFLPDdoMain [instanceFile] [maxWidth]
 * }</pre>
 * - {@code instanceFile} (optional): Path to the SRFLP instance file. Defaults to {@code data/SRFLP/simple}.
 * - {@code maxWidth} (optional): Maximum width of the relaxed decision diagram. Defaults to 50.
 *
 * <p>
 * The DDO model requires the following components:
 * </p>
 * <ul>
 *     <li>{@link SRFLPProblem} – the problem definition (distance/cost matrix, number of facilities, etc.),</li>
 *     <li>{@link SRFLPRelax} – relaxation method used to merge states in the diagram,</li>
 *     <li>{@link SRFLPRanking} – state ranking used for node prioritization,</li>
 *     <li>{@link FixedWidth} – width control heuristic for the relaxed diagram,</li>
 *     <li>{@link SRFLPFastLowerBound} – fast lower-bound estimator for pruning.</li>
 * </ul>
 *
 * <p>
 * After the search is completed, the program prints:
 * </p>
 * <ul>
 *     <li>Search statistics returned by {@link Solvers#minimizeDdo},</li>
 *     <li>The best solution found as an array of facility indices.</li>
 * </ul>
 *
 * @see SRFLPProblem
 * @see SRFLPState
 * @see SRFLPRelax
 * @see SRFLPRanking
 * @see FixedWidth
 * @see SRFLPFastLowerBound
 * @see DdoModel
 * @see Solvers
 */
public final class SRFLPDdoMain {

    public static void main(String[] args) throws IOException {
        final String filename = args.length == 0 ? Paths.get("data", "SRFLP", "simple").toString() :
                args[0];
        final int maxWidth = args.length > 1 ? Integer.parseInt(args[1]) : 50;

        final SRFLPProblem problem = new SRFLPProblem(filename);

        DdoModel<SRFLPState> model = new DdoModel<>() {
            @Override
            public Problem<SRFLPState> problem() {
                return problem;
            }

            @Override
            public Relaxation<SRFLPState> relaxation() {
                return new SRFLPRelax(problem);
            }

            @Override
            public StateRanking<SRFLPState> ranking() {
                return new SRFLPRanking();
            }

            @Override
            public WidthHeuristic<SRFLPState> widthHeuristic() {
                return new FixedWidth<>(maxWidth);
            }

            @Override
            public FastLowerBound<SRFLPState> lowerBound() {
                return new SRFLPFastLowerBound(problem);
            }
        };

        int[] bestSolution = new int[problem.nbVars()];

        SearchStatistics finalStats = Solvers.minimizeDdo(model, (sol, stat) -> {
            SolutionPrinter.printSolution(stat, sol);
            System.arraycopy(sol, 0, bestSolution, 0, sol.length);
        });

        System.out.println("\n");
        System.out.println("===== Optimal Solution =====");
        System.out.println(finalStats);
        System.out.println("Best solution: " + Arrays.toString(bestSolution));
    }
}
