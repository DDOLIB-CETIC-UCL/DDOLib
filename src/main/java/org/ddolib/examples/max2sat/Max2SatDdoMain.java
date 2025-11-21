package org.ddolib.examples.max2sat;

import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.modeling.DdoModel;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Relaxation;
import org.ddolib.modeling.Solvers;
import org.ddolib.util.io.SolutionPrinter;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Maximum 2-Satisfiability (MAX2SAT) (MAX2SAT) problem with Ddo.
 * Entry point for solving the <b>Maximum 2-Satisfiability (MAX2SAT)</b> problem
 * using the <b>Decision Diagram Optimization (DDO)</b> algorithm.
 * <p>
 * The <b>DDO algorithm</b> incrementally constructs and explores a
 * decision diagram that represents all feasible partial assignments.
 * It prunes suboptimal regions using relaxation and ranking heuristics,
 * while maintaining lower bounds to ensure convergence toward the optimal solution.
 * </p>
 *
 * <p><b>Usage:</b></p>
 * <ul>
 *   <li>If no command-line argument is provided, the default instance file
 *   <code>data/Max2Sat/wcnf_var_4_opti_39.txt</code> is used.</li>
 *   <li>Otherwise, the first argument should specify the path to a Weighted CNF (WCNF) instance file.</li>
 * </ul>
 *
 * <p><b>Example:</b></p>
 * <pre>{@code
 * java Max2SatDdoMain data/Max2Sat/wcnf_var_20_opti_110.txt
 * }</pre>
 *
 * <p>
 * The program prints each new incumbent solution found during the search using
 * the {@link SolutionPrinter}, followed by the final {@link SearchStatistics},
 * which summarize key performance metrics such as explored nodes, best objective value,
 * and total computation time.
 * </p>
 *
 * @see Max2SatProblem
 * @see Max2SatState
 * @see Max2SatRelax
 * @see Max2SatRanking
 * @see Max2SatFastLowerBound
 * @see DdoModel
 * @see Solvers#minimizeDdo(DdoModel, java.util.function.BiConsumer)
 * @see SearchStatistics
 */
public final class Max2SatDdoMain {

    /**
     * Main entry point for executing the DDO algorithm on a MAX2SAT instance.
     *
     * @param args optional command-line argument specifying the path to the WCNF instance file;
     *             if omitted, a default instance file is used.
     * @throws IOException if the instance file cannot be found, opened, or parsed.
     */
    public static void main(String[] args) throws IOException {
        // Select instance file: default or provided path
        String instance = args.length == 0
                ? Path.of("data", "Max2Sat", "wcnf_var_4_opti_39.txt").toString()
                : args[0];

        // Load MAX2SAT instance
        final Max2SatProblem problem = new Max2SatProblem(instance);

        // Define DDO model for MAX2SAT
        DdoModel<Max2SatState> model = new DdoModel<>() {
            @Override
            public Problem<Max2SatState> problem() {
                return problem;
            }

            @Override
            public Relaxation<Max2SatState> relaxation() {
                return new Max2SatRelax(problem);
            }

            @Override
            public Max2SatRanking ranking() {
                return new Max2SatRanking();
            }

            @Override
            public Max2SatFastLowerBound lowerBound() {
                return new Max2SatFastLowerBound(problem);
            }
        };

        // Execute DDO search and print intermediate solutions
        SearchStatistics stats = Solvers.minimizeDdo(model, (sol, s) -> {
            SolutionPrinter.printSolution(s, sol);
        });

        // Display search statistics
        System.out.println(stats);
    }
}
