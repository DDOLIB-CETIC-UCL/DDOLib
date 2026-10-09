package org.ddolib.examples.layered.msct;

import java.io.IOException;
import java.nio.file.Path;
import org.ddolib.common.frontier.CutSetType;
import org.ddolib.common.frontier.Frontier;
import org.ddolib.common.frontier.SimpleFrontier;
import org.ddolib.common.heuristics.width.FixedWidth;
import org.ddolib.common.heuristics.width.WidthHeuristic;
import org.ddolib.common.util.io.SolutionPrinter;
import org.ddolib.layered.modeling.DdoModel;
import org.ddolib.layered.modeling.DominanceChecker;
import org.ddolib.layered.modeling.FastLowerBound;
import org.ddolib.layered.modeling.Problem;
import org.ddolib.layered.modeling.SimpleDominanceChecker;
import org.ddolib.layered.modeling.Solvers;
import org.ddolib.layered.solver.Solution;

/**
 * Minimum Sum Completion Time (MSCT) with Ddo. Main class for solving the Maximum Sum of Compatible
 * Tasks (MSCT) problem using the Decision Diagram Optimization (DDO) approach.
 *
 * <p>This implementation constructs a DDO model for the MSCT problem, which defines:
 *
 * <ul>
 *   <li>The problem instance ({@link MSCTProblem})
 *   <li>A relaxation model ({@link MSCTRelax})
 *   <li>A ranking strategy for state exploration ({@link MSCTRanking})
 *   <li>A dominance checker to remove dominated states ({@link MSCTDominance})
 *   <li>A frontier manager controlling active nodes in the diagram
 *   <li>A fixed-width heuristic limiting the number of nodes per layer
 *   <li>A fast lower bound estimator ({@link MSCTFastLowerBound})
 * </ul>
 *
 * <p>The solver minimizes the given model using the DDO-based method provided by {@link
 * Solvers#minimizeDdo(DdoModel, java.util.function.BiConsumer)} and prints the best solution found
 * along with statistics about the search process.
 *
 * <p><b>Usage:</b>
 *
 * <pre>
 * java MSCTDdoMain [instance_file]
 * </pre>
 *
 * <p>If no argument is provided, the default instance {@code data/MSCT/msct1.txt} is used.
 *
 * <p><b>Example:</b>
 *
 * <pre>
 * java MSCTDdoMain data/MSCT/sample_instance.txt
 * </pre>
 */
public class MSCTDdoMain {

    private MSCTDdoMain() {}

    /**
     * Entry point of the program.
     *
     * <p>This method loads the MSCT problem instance, builds the DDO model with the appropriate
     * relaxation, ranking, dominance, and lower bound strategies, and runs the DDO solver to find
     * the minimum-cost solution.
     *
     * @param args optional command-line arguments; if provided, the first argument specifies the
     *     path to the instance file of the MSCT problem.
     * @throws IOException if an error occurs while reading the problem instance file
     */
    public static void main(final String[] args) throws IOException {
        final String instance =
                args.length == 0 ? Path.of("data", "MSCT", "msct1.txt").toString() : args[0];
        final MSCTProblem problem = new MSCTProblem(instance);
        DdoModel<MSCTState> model =
                new DdoModel<>() {
                    @Override
                    public Problem<MSCTState> problem() {
                        return problem;
                    }

                    @Override
                    public FastLowerBound<MSCTState> lowerBound() {
                        return new MSCTFastLowerBound(problem);
                    }

                    @Override
                    public DominanceChecker<MSCTState> dominance() {
                        return new SimpleDominanceChecker<>(new MSCTDominance(), problem.nbVars());
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
                    public WidthHeuristic<MSCTState> widthHeuristic() {
                        return new FixedWidth<>(100);
                    }

                    @Override
                    public Frontier<MSCTState> frontier() {
                        return new SimpleFrontier<>(ranking(), CutSetType.Frontier);
                    }

                    @Override
                    public boolean useCache() {
                        return true;
                    }
                };

        Solution bestSolution =
                Solvers.minimizeDdo(
                        model,
                        (sol, s) -> {
                            SolutionPrinter.printSolution(s, sol);
                        });

        System.out.println(bestSolution.statistics());
        System.out.println(bestSolution);
    }
}
