package org.ddolib.examples.maximumcoverage;

import org.ddolib.common.solver.Solution;
import org.ddolib.ddo.core.heuristics.cluster.CostBased;
import org.ddolib.ddo.core.heuristics.cluster.GHP;
import org.ddolib.ddo.core.heuristics.cluster.ReductionStrategy;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.modeling.DdoModel;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solvers;
import org.ddolib.util.io.SolutionPrinter;

import java.io.IOException;
/**
 * Maximum Coverage (MaxCover) problem with Ddo
 * <p>
 * This class demonstrates how to solve an instance of the Maximum Coverage (MaxCover) problem (BKP)
 * using a Decision Diagram Optimization (DDO) algorithm.
 * <ul>
 *   <li>Builds an instance of the MaxCover problem (randomly generated or loaded from a file)</li>
 *   <li>Defines a {@link DdoModel} by specifying the problem, relaxation,
 *       ranking strategy, width heuristic, and lower bound</li>
 *   <li>Runs the DDO solver to compute a solution</li>
 *   <li>Prints the resulting solution</li>
 * </ul>
 *
 * <p>
 * The problem parameters (size, cardinality, random seed, etc.) can be
 * easily modified to experiment with different instances.
 */
public class MaxCoverDdoMainWithCluster {
    public static void main(String[] args) throws IOException {
        /**
         * Program entry point.
         *
         * <p>
         * This method:
         * <ol>
         *   <li>Creates an instance of the MaxCover problem</li>
         *   <li>Builds a DDO model by defining:
         *     <ul>
         *       <li>the problem to solve</li>
         *       <li>the relaxation used during search</li>
         *       <li>the state ranking strategy</li>
         *       <li>the width heuristic (fixed width in this example)</li>
         *       <li>a fast lower bound</li>
         *     </ul>
         *   </li>
         *   <li>Runs the DDO solver in minimization mode</li>
         *   <li>Prints the intermediate and final solutions</li>
         * </ol>
         *
         * @param args command-line arguments (not used)
         * @throws IOException if an error occurs while loading a problem instance from a file
         */
        MaxCoverProblem problem = new MaxCoverProblem(30, 30, 7,0.1,42);
        // MaxCoverProblem problem = new MaxCoverProblem(10, 10, 5,0.1,42);

        // MaxCoverProblem problem = new MaxCoverProblem("src/test/resources/MaxCover/mc_n10_m5_k3_r10_0.txt");
        System.out.println(problem);
        DdoModel<MaxCoverState> model = new DdoModel<>() {
            @Override
            public Problem<MaxCoverState> problem() {
                return problem;
            }

            @Override
            public MaxCoverRelax relaxation() {
                return new MaxCoverRelax(problem);
            }

            @Override
            public MaxCoverRanking ranking() {
                return new MaxCoverRanking();
            }

            @Override
            public WidthHeuristic<MaxCoverState> widthHeuristic() {
                return new FixedWidth<>(100);
            }

            @Override
            public MaxCoverFastLowerBound lowerBound() {
                return new MaxCoverFastLowerBound(problem);
            }

            @Override
            public boolean exportDot() {
                return false;
            }

            @Override
            public ReductionStrategy<MaxCoverState> relaxStrategy() {
                return new GHP<>(new MaxCoverDistance(problem));
//                return new Hybrid<>(new MaxCoverRanking(), new MaxCoverDistance(problem));
//                return new CostBased<>(new MaxCoverRanking()); // default strategy
            }

            @Override
            public ReductionStrategy<MaxCoverState> restrictStrategy() {
                return new GHP<>(new MaxCoverDistance(problem));
//                return new Hybrid<>(new MaxCoverRanking(), new MaxCoverDistance(problem));
//                return new CostBased<>(new MaxCoverRanking()); // default strategy
            }
        };

        Solution solution = Solvers.minimizeDdo(model, (sol, s) -> {
            SolutionPrinter.printSolution(s,sol);
        });
        System.out.println();
        System.out.println(solution);
    }

}
