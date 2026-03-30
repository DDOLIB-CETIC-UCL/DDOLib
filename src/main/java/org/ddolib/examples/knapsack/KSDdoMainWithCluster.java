package org.ddolib.examples.knapsack;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.common.solver.Solution;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.Frontier;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.cluster.GHP;
import org.ddolib.ddo.core.heuristics.cluster.ReductionStrategy;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.modeling.*;
import org.ddolib.util.io.SolutionPrinter;
import org.ddolib.util.verbosity.VerbosityLevel;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Knapsack Problem (KS) with Ddo.
 * <p>
 * This class demonstrates how to solve an instance of the bounded Knapsack Problem (BKP)
 * using a Decision Diagram Optimization (DDO) algorithm.
 * </p>
 * <p>
 * The program performs the following steps:
 * </p>
 * <ol>
 *     <li>Loads a knapsack instance from a data file.</li>
 *     <li>Defines a {@link DdoModel} with:
 *         <ul>
 *             <li>A relaxation function for merging states ({@link KSRelax}).</li>
 *             <li>A state ranking heuristic ({@link KSRanking}).</li>
 *             <li>A fast lower bound heuristic ({@link KSFastLowerBound}).</li>
 *             <li>A dominance checker ({@link KSDominance}).</li>
 *             <li>A frontier of type {@link CutSetType#Frontier} ({@link SimpleFrontier}).</li>
 *             <li>A width heuristic ({@link FixedWidth}) and cache usage.</li>
 *             <li>Verbose output level ({@link VerbosityLevel}).</li>
 *         </ul>
 *     </li>
 *     <li>Creates a {@link Solvers} and runs the DDO algorithm.</li>
 *     <li>Prints updates when a new incumbent solution is found and stops after 10 seconds of runtime.</li>
 *     <li>Outputs the final search statistics.</li>
 * </ol>
 *
 * <p>
 * The DDO solver leverages relaxed decision diagrams, caching, and state ranking to
 * efficiently explore the search space for high-quality solutions.
 * </p>
 */
public class KSDdoMainWithCluster {
    /**
     * Entry point of the DDO demonstration for the Knapsack Problem.
     *
     * @param args command-line arguments (not used)
     * @throws IOException if the instance file cannot be read
     */
    public static void main(final String[] args) throws IOException {
        final String instance = args.length == 0 ? Path.of("data", "Knapsack", "instance_n1000_c1000_10_5_10_5_0").toString() : args[0];
        final KSProblem problem = new KSProblem(instance);
        final DdoModel<Integer> model = new DdoModel<>() {
            @Override
            public Problem<Integer> problem() {
                return problem;
            }

            @Override
            public FastLowerBound<Integer> lowerBound() {
                return new KSFastLowerBound(problem);
            }

            @Override
            public DominanceChecker<Integer> dominance() {
                return new SimpleDominanceChecker<>(new KSDominance(), problem.nbVars());
            }

            @Override
            public VerbosityLevel verbosityLevel() {
                return VerbosityLevel.LARGE;
            }

            @Override
            public Relaxation<Integer> relaxation() {
                return new KSRelax();
            }

            @Override
            public KSRanking ranking() {
                return new KSRanking();
            }

            @Override
            public WidthHeuristic<Integer> widthHeuristic() {
                return new FixedWidth<>(100);
            }

            @Override
            public Frontier<Integer> frontier() {
                return new SimpleFrontier<>(ranking(), CutSetType.Frontier);
            }

            @Override
            public boolean useCache() {
                return true;
            }

            @Override
            public ReductionStrategy<Integer> relaxStrategy() {
                return new GHP<>(new KSDistance(problem));
//                return new Hybrid<>(new KSRanking(), new KSDistance(problem));
//                return new CostBased<>(new KSRanking());  // default strategy
            }

            @Override
            public ReductionStrategy<Integer> restrictStrategy() {
                return new GHP<>(new KSDistance(problem));
//                return new Hybrid<>(new KSRanking(), new KSDistance(problem));
//                return new CostBased<>(new KSRanking()); // default strategy
            }
        };

        Solution bestSolution = Solvers.minimizeDdo(model, (sol, s) -> {
            SolutionPrinter.printSolution(s, sol);
        });

        System.out.println(bestSolution.statistics());
        System.out.println(bestSolution);


    }
}
