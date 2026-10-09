package org.ddolib.examples.layered.gruler;

import java.io.IOException;
import org.ddolib.common.heuristics.width.FixedWidth;
import org.ddolib.common.heuristics.width.WidthHeuristic;
import org.ddolib.common.util.io.SolutionPrinter;
import org.ddolib.layered.modeling.DdoModel;
import org.ddolib.layered.modeling.FastLowerBound;
import org.ddolib.layered.modeling.Problem;
import org.ddolib.layered.modeling.Relaxation;
import org.ddolib.layered.modeling.Solvers;
import org.ddolib.layered.modeling.StateRanking;
import org.ddolib.layered.solver.Solution;

/**
 * Golomb Rule Problem (GRP) with Ddo. Main class for solving the Golomb Ruler Problem (GRP) using a
 * Dynamic Discrete Optimization (DDO) approach.
 *
 * <p>This class demonstrates how to create a Golomb Ruler problem instance, define a DDO model with
 * relaxation and state ranking, and solve the problem using {@link Solvers#minimizeDdo(DdoModel,
 * java.util.function.BiConsumer)}. The solution and search statistics are printed to the console.
 */
public class GRDdoMain {

    private GRDdoMain() {}

    /**
     * Entry point of the application.
     *
     * <p>The method performs the following steps:
     *
     * <ol>
     *   <li>Creates a Golomb Ruler problem instance with 9 marks.
     *   <li>Defines a DDO model for the problem, including:
     *       <ul>
     *         <li>Relaxation using {@link GRRelax}
     *         <li>State ranking using {@link GRRanking}
     *       </ul>
     *   <li>Solves the problem using the DDO solver.
     *   <li>Prints the solution and search statistics to the console.
     * </ol>
     *
     * @param args command-line arguments (not used)
     * @throws IOException if an I/O error occurs while printing the solution
     */
    public static void main(final String[] args) throws IOException {
        GRProblem problem = new GRProblem(9);
        final DdoModel<GRState> model =
                new DdoModel<>() {
                    @Override
                    public Problem<GRState> problem() {
                        return problem;
                    }

                    @Override
                    public FastLowerBound<GRState> lowerBound() {
                        return new GRFastLowerBound();
                    }

                    @Override
                    public Relaxation<GRState> relaxation() {
                        return new GRRelax();
                    }

                    @Override
                    public StateRanking<GRState> ranking() {
                        return new GRRanking();
                    }

                    @Override
                    public WidthHeuristic<GRState> widthHeuristic() {
                        return new FixedWidth<>(500);
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
