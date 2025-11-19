package org.ddolib.examples.msct;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.Frontier;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.modeling.DdoModel;
import org.ddolib.modeling.FastLowerBound;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solvers;
import org.ddolib.util.io.SolutionPrinter;


import java.io.IOException;
import java.nio.file.Path;

/**
 *  Minimum Sum Completion Time (MSCT) with Ddo.
 * Main class for solving the Maximum Sum of Compatible Tasks (MSCT) problem
 * using the Decision Diagram Optimization (DDO) approach.
 * <p>
 * This implementation constructs a DDO model for the MSCT problem, which defines:
 * </p>
 * <ul>
 *     <li>The problem instance ({@link MSCTProblem})</li>
 *     <li>A relaxation model ({@link MSCTRelax})</li>
 *     <li>A ranking strategy for state exploration ({@link MSCTRanking})</li>
 *     <li>A dominance checker to remove dominated states ({@link MSCTDominance})</li>
 *     <li>A frontier manager controlling active nodes in the diagram</li>
 *     <li>A fixed-width heuristic limiting the number of nodes per layer</li>
 *     <li>A fast lower bound estimator ({@link MSCTFastLowerBound})</li>
 * </ul>
 * The solver minimizes the given model using the DDO-based method provided by
 * {@link Solvers#minimizeDdo(DdoModel, java.util.function.BiConsumer)} and prints
 * the best solution found along with statistics about the search process.
 *
 * <p><b>Usage:</b></p>
 * <pre>
 * java MSCTDdoMain [instance_file]
 * </pre>
 * If no argument is provided, the default instance {@code data/MSCT/msct1.txt} is used.
 *
 * <p><b>Example:</b></p>
 * <pre>
 * java MSCTDdoMain data/MSCT/sample_instance.txt
 * </pre>
 */
public class MSCTDdoMain {
    /**
     * Entry point of the program.
     * <p>
     * This method loads the MSCT problem instance, builds the DDO model with
     * the appropriate relaxation, ranking, dominance, and lower bound strategies,
     * and runs the DDO solver to find the minimum-cost solution.
     * </p>
     *
     * @param args optional command-line arguments; if provided, the first argument specifies
     *             the path to the instance file of the MSCT problem.
     * @throws IOException if an error occurs while reading the problem instance file.
     */
    public static void main(final String[] args) throws IOException {
        final String instance = args.length == 0 ? Path.of("data","MSCT","msct1.txt").toString() : args[0];
        final MSCTProblem problem = new MSCTProblem(instance);
        DdoModel<MSCTState> model = new DdoModel<>() {
            @Override
            public Problem<MSCTState> problem() {
                return problem;
            }

            @Override
            public MSCTRelax relaxation() {
                return new MSCTRelax(problem);
            }

            @Override
            public MSCTRanking ranking() {
                return new MSCTRanking();
            }

            @Override
            public DominanceChecker<MSCTState> dominance() {
                return new SimpleDominanceChecker<>(new MSCTDominance(), problem.nbVars());
            }
            @Override
            public Frontier<MSCTState> frontier() {
                return new SimpleFrontier<>(ranking(), CutSetType.Frontier);
            }

            @Override
            public WidthHeuristic<MSCTState> widthHeuristic() {
                return new FixedWidth<>(100);
            }
            @Override
            public FastLowerBound<MSCTState> lowerBound() {
                return new MSCTFastLowerBound(problem);
            }

            @Override
            public boolean useCache() {
                return true;
            }
        };

        SearchStatistics stats = Solvers.minimizeDdo(model, (sol, s) -> {
            SolutionPrinter.printSolution(s,sol);
        });

        System.out.println(stats);
    }
}


