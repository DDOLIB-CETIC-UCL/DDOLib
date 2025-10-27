package org.ddolib.examples.gruler;

import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.modeling.*;
import org.ddolib.util.io.SolutionPrinter;

import java.io.IOException;

/**
 * Golomb Rule Problem (GRP) with Ddo.
 * Main class for solving the Golomb Ruler Problem (GRP) using a Dynamic Discrete Optimization (DDO) approach.
 * <p>
 * This class demonstrates how to create a Golomb Ruler problem instance, define a DDO model with relaxation
 * and state ranking, and solve the problem using {@link Solvers#minimizeDdo(DdoModel, java.util.function.BiConsumer)}.
 * The solution and search statistics are printed to the console.
 * </p>
 */
public class GRDdoMain {
    /**
     * Entry point of the application.
     * <p>
     * The method performs the following steps:
     * </p>
     * <ol>
     *     <li>Creates a Golomb Ruler problem instance with 9 marks.</li>
     *     <li>Defines a DDO model for the problem, including:
     *         <ul>
     *             <li>Relaxation using {@link GRRelax}</li>
     *             <li>State ranking using {@link GRRanking}</li>
     *         </ul>
     *     </li>
     *     <li>Solves the problem using the DDO solver.</li>
     *     <li>Prints the solution and search statistics to the console.</li>
     * </ol>
     * @param args command-line arguments (not used)
     * @throws IOException if an I/O error occurs while printing the solution
     */
    public static void main(final String[] args) throws IOException {
        GRProblem problem = new GRProblem(9);
        final DdoModel<GRState> model = new DdoModel<>() {
            @Override
            public Problem<GRState> problem() {
                return problem;
            }

            @Override
            public Relaxation<GRState> relaxation() {
                return new GRRelax();
            }

            @Override
            public StateRanking<GRState> ranking() {
                return new GRRanking();
            }
        };

        SearchStatistics stats = Solvers.minimizeDdo(model, (sol, s) -> {
            SolutionPrinter.printSolution(s,sol);
        });

        System.out.println(stats);
    }
}
