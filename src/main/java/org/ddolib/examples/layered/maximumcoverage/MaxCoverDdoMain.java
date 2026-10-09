package org.ddolib.examples.layered.maximumcoverage;

import java.io.IOException;
import org.ddolib.common.heuristics.width.FixedWidth;
import org.ddolib.common.heuristics.width.WidthHeuristic;
import org.ddolib.common.util.io.SolutionPrinter;
import org.ddolib.layered.modeling.DdoModel;
import org.ddolib.layered.modeling.Problem;
import org.ddolib.layered.modeling.Solvers;
import org.ddolib.layered.solver.Solution;

/**
 * Maximum Coverage (MaxCover) problem with Ddo
 *
 * <p>This class demonstrates how to solve an instance of the Maximum Coverage (MaxCover) problem
 * (BKP) using a Decision Diagram Optimization (DDO) algorithm.
 *
 * <ul>
 *   <li>Builds an instance of the MaxCover problem (randomly generated or loaded from a file)
 *   <li>Defines a {@link DdoModel} by specifying the problem, relaxation, ranking strategy, width
 *       heuristic, and lower bound
 *   <li>Runs the DDO solver to compute a solution
 *   <li>Prints the resulting solution
 * </ul>
 *
 * <p>The problem parameters (size, cardinality, random seed, etc.) can be easily modified to
 * experiment with different instances.
 */
public class MaxCoverDdoMain {

    private MaxCoverDdoMain() {}

    /**
     * Program entry point.
     *
     * <p>This method:
     *
     * <ol>
     *   <li>Creates an instance of the MaxCover problem
     *   <li>Builds a DDO model by defining:
     *       <ul>
     *         <li>the problem to solve
     *         <li>the relaxation used during search
     *         <li>the state ranking strategy
     *         <li>the width heuristic (fixed width in this example)
     *         <li>a fast lower bound
     *       </ul>
     *   <li>Runs the DDO solver in minimization mode
     *   <li>Prints the intermediate and final solutions
     * </ol>
     *
     * @param args command-line arguments (not used)
     * @throws IOException if an error occurs while loading a problem instance from a file
     */
    public static void main(String[] args) throws IOException {
        MaxCoverProblem problem = new MaxCoverProblem(30, 30, 7, 0.1, 42);
        // MaxCoverProblem problem = new MaxCoverProblem(10, 10, 5,0.1,42);

        // MaxCoverProblem problem = new
        // MaxCoverProblem("src/test/resources/MaxCover/mc_n10_m5_k3_r10_0.txt");
        System.out.println(problem);
        DdoModel<MaxCoverState> model =
                new DdoModel<>() {
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
                        return new FixedWidth<>(10);
                    }

                    @Override
                    public MaxCoverFastLowerBound lowerBound() {
                        return new MaxCoverFastLowerBound(problem);
                    }

                    @Override
                    public boolean exportDot() {
                        return false;
                    }
                };

        Solution solution =
                Solvers.minimizeDdo(
                        model,
                        (sol, s) -> {
                            SolutionPrinter.printSolution(s, sol);
                        });
        System.out.println();
        System.out.println(solution);
    }
}
