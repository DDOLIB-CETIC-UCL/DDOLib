package org.ddolib.examples.smic;

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
 * Entry point for solving the Single Machine with Inventory Constraint (SMIC) problem
 * using a Large Neighborhood Search (LNS) approach.
 *
 * <p>This class reads an instance file describing an SMIC problem, constructs an
 * LNS model, and attempts to find an optimal or near-optimal solution within a
 * time limit. The solution is printed along with runtime statistics.</p>
 *
 * <p>Usage:</p>
 * <pre>
 * java SMICLnsMain [instanceFilePath]
 * </pre>
 * <ul>
 *     <li>If no {@code instanceFilePath} is provided, a default instance
 *     located at {@code data/SMIC/data10_1.txt} is used.</li>
 *     <li>The program runs a time-limited LNS search (1 second) and prints
 *     the best solution found along with statistics.</li>
 * </ul>
 *
 * <p>The LNS model is configured with:</p>
 * <ul>
 *     <li>A {@link SMICFastLowerBound} for fast lower-bound estimation.</li>
 *     <li>A {@link SimpleDominanceChecker} using {@link SMICDominance} to prune dominated states.</li>
 *     <li>A {@link SMICRanking} to rank decisions in the search.</li>
 *     <li>A fixed width heuristic ({@link FixedWidth}) with width = 2 for tree exploration.</li>
 *     <li>Export of DOT files for visualization of the search tree.</li>
 * </ul>
 *
 * <p>The final solution and statistics are printed to {@code System.out}.</p>
 *
 * @author
 * @version 1.0
 */
public class SMICLnsMain {
    /**
     * Main method to run the SMIC LNS solver.
     *
     * @param args optional command-line argument specifying the path to the SMIC instance file.
     *             If omitted, the default instance {@code data/SMIC/data10_1.txt} is used.
     * @throws IOException if there is an error reading the instance file.
     */
    public static void main(String[] args) throws IOException {
        final String instance = args.length == 0 ? Path.of("data", "SMIC", "data10_1.txt").toString() : args[0];
        final SMICProblem problem = new SMICProblem(instance);
        LnsModel<SMICState> model = new LnsModel<>() {
            @Override
            public Problem<SMICState> problem() {
                return problem;
            }

            @Override
            public SMICFastLowerBound lowerBound() {
                return new SMICFastLowerBound(problem);
            }

            @Override
            public DominanceChecker<SMICState> dominance() {
                return new SimpleDominanceChecker<>(new SMICDominance(), problem.nbVars());
            }

            @Override
            public SMICRanking ranking() {
                return new SMICRanking();
            }

            @Override
            public WidthHeuristic<SMICState> widthHeuristic() {
                return new FixedWidth<>(2);
            }

            @Override
            public boolean exportDot() {
                return true;
            }
        };
        Solution bestSolution = Solvers.minimizeLns(model, s -> s.runtime() < 1000, (sol, s) -> {
            SolutionPrinter.printSolution(s, sol);
        });

        System.out.println(bestSolution.statistics());
        System.out.println(bestSolution);
    }
}