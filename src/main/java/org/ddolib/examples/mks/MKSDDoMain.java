package org.ddolib.examples.mks;

import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.common.solver.Solution;
import org.ddolib.ddo.core.heuristics.cluster.*;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.examples.mks.*;
import org.ddolib.modeling.DdoModel;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solvers;
import org.ddolib.util.io.SolutionPrinter;

import java.io.IOException;
import java.nio.file.Path;
/**
 * Main class to demonstrate the application of Decision Diagram Optimization (DDO)
 * on a Multi-dimensional Knapsack (MKS) problem instance.
 *
 * <p>
 * This class:
 * <ul>
 *     <li>Loads an MKS problem from a file.</li>
 *     <li>Defines a {@link DdoModel} for solving the problem using a relaxed decision diagram.</li>
 *     <li>Specifies state ranking, relaxation, width heuristic, and reduction strategies.</li>
 * </ul>
 *
 * <p>
 * The example shows how to combine different reduction strategies (e.g., {@link GHP}, {@link CostBased}, {@link Hybrid})
 * and how to customize the width of the decision diagram.
 */
public class MKSDDoMain {
    /**
     * Entry point for the DDO demonstration.
     *
     * @param args command-line arguments (not used)
     * @throws IOException if the problem file cannot be read
     */
    public static void main(String[] args) throws IOException {
        // Load the MKS problem from a file
        MKSProblem problem = new MKSProblem(Path.of("data", "MKS", "or-library", "mknapcb1_1.txt").toString());
        // Define a DDO model for the problem
        DdoModel<MKSState> model = new DdoModel<>() {
            @Override
            public Problem<MKSState> problem() {
                return problem;
            }

            @Override
            public MKSRelax relaxation() {
                return new MKSRelax();
            }

            @Override
            public MKSRanking ranking() {
                return new MKSRanking();
            }

            @Override
            public WidthHeuristic<MKSState> widthHeuristic() {
                return new FixedWidth<>(100);
            }

            @Override
            public boolean exportDot() {
                return true;
            }

            @Override
            public ReductionStrategy<MKSState> relaxStrategy() {
                return new GHP<>(new MKSDistance(problem));
//                return new Hybrid<>(new MKSRanking(), new MKSDistance(problem));
//                return new CostBased<>(new MKSRanking());
            }

            @Override
            public ReductionStrategy<MKSState> restrictStrategy() {
                return new GHP<>(new MKSDistance(problem));
//                return new Hybrid<>(new MKSRanking(), new MKSDistance(problem));
//                return new CostBased<>(new MKSRanking());
            }
        };
        // Solve the problem and print the solution
        Solution solution = Solvers.minimizeDdo(model, (sol, s) -> {
            SolutionPrinter.printSolution(s,sol);
        });
        System.out.println();
        System.out.println(solution);
    }

}
