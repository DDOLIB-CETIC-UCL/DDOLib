package org.ddolib.examples.boundedknapsack;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.modeling.DdoModel;
import org.ddolib.modeling.Solvers;
import org.ddolib.util.io.SolutionPrinter;

/**
 * Bounded Knapsack Problem (BKS) with Ddo.
 * Main class for solving the Bounded Knapsack Problem (BKS) using a Decision Diagram Optimization (DDO) approach.
 * <p>
 * This class demonstrates how to set up a BKS problem instance, define a DDO model with relaxation,
 * ranking, lower bound, dominance checker, width heuristic, and frontier, and solve the problem using
 * {@link Solvers#minimizeDdo(DdoModel, java.util.function.BiConsumer)}. The solution and statistics are printed to the console.
 * </p>
 */
public class BKSDdoMain {
    /**
     * Entry point of the application.
     * <p>
     * The method performs the following steps:
     * </p>
     * <ol>
     *     <li>Creates a BKS problem instance with 100 items, capacity 1000, and strongly correlated instance type.</li>
     *     <li>Defines a DDO model for the problem, including:
     *         <ul>
     *             <li>Relaxation using {@link BKSRelax}</li>
     *             <li>Ranking using {@link BKSRanking}</li>
     *             <li>Lower bound using {@link BKSFastLowerBound}</li>
     *             <li>Dominance checker using {@link SimpleDominanceChecker} and {@link BKSDominance}</li>
     *             <li>Cache enabled</li>
     *             <li>Width heuristic using {@link FixedWidth}</li>
     *             <li>Frontier using {@link SimpleFrontier} and {@link CutSetType#Frontier}</li>
     *         </ul>
     *     </li>
     *     <li>Solves the problem using the DDO solver.</li>
     *     <li>Prints the solution and search statistics to the console.</li>
     * </ol>
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        final BKSProblem problem = new BKSProblem(100, 1000, BKSProblem.InstanceType.STRONGLY_CORRELATED, 0);
        DdoModel<Integer> model = new DdoModel<>() {
            @Override
            public BKSProblem problem() {
                return problem;
            }

            @Override
            public BKSRelax relaxation() {
                return new BKSRelax();
            }

            @Override
            public BKSRanking ranking() {
                return new BKSRanking();
            }

            @Override
            public BKSFastLowerBound lowerBound() {
                return new BKSFastLowerBound(problem);
            }

            @Override
            public DominanceChecker<Integer> dominance() {
                return new SimpleDominanceChecker<Integer>(new BKSDominance(), problem.nbVars());
            }

            @Override
            public boolean useCache() {
                return true;
            }

            @Override
            public WidthHeuristic<Integer> widthHeuristic() {
                return new FixedWidth<>(100);
            }

            @Override
            public SimpleFrontier<Integer> frontier() {
                return new SimpleFrontier<>(ranking(), CutSetType.Frontier);
            }
        };

        SearchStatistics stats = Solvers.minimizeDdo(model, (sol, s) -> {
            SolutionPrinter.printSolution(s,sol);
        });

        System.out.println(stats);
    }
}

