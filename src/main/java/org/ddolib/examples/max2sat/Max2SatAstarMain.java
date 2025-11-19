package org.ddolib.examples.max2sat;

import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.modeling.AcsModel;
import org.ddolib.modeling.Model;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solvers;
import org.ddolib.util.io.SolutionPrinter;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Maximum 2-Satisfiability (MAX2SAT) (MAX2SAT) problem with AsTar.
 * Entry point for solving the <b>Maximum 2-Satisfiability (MAX2SAT)</b> problem
 * using the <b>A*</b> search algorithm.
 *
 * <p>
 * The <b>A*</b> algorithm explores the search space by expanding states in order
 * of increasing estimated total cost (i.e., actual cost + heuristic lower bound).
 * It guarantees finding an optimal solution when the heuristic is admissible.
 * </p>
 *
 * <p><b>Usage:</b></p>
 * <ul>
 *     <li>If no command-line argument is provided, the default instance file
 *     <code>data/Max2Sat/wcnf_var_4_opti_39.txt</code> is used.</li>
 *     <li>Otherwise, the first argument should specify the path to a Weighted CNF (WCNF) instance file.</li>
 * </ul>
 *
 * <p><b>Example:</b></p>
 * <pre>{@code
 * java Max2SatAstarMain data/Max2Sat/wcnf_var_20_opti_110.txt
 * }</pre>
 *
 * <p>
 * The program prints each solution found by the A* algorithm via the
 * {@link SolutionPrinter} utility, followed by overall search statistics
 * such as number of explored nodes, best cost, and runtime.
 * </p>
 *
 * @see Max2SatProblem
 * @see Max2SatState
 * @see Max2SatFastLowerBound
 * @see Solvers#minimizeAstar(Model, java.util.function.BiConsumer)
 * @see SearchStatistics
 */
public final class Max2SatAstarMain {

    /**
     * Main entry point for executing the A* search on a MAX2SAT instance.
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

        // Define A* model for MAX2SAT
        Model<Max2SatState> model = new Model<>() {
            @Override
            public Problem<Max2SatState> problem() {
                return problem;
            }

            @Override
            public Max2SatFastLowerBound lowerBound() {
                return new Max2SatFastLowerBound(problem);
            }
        };

        // Launch A* search and print intermediate solutions
        SearchStatistics stats = Solvers.minimizeAstar(model, (sol, s) -> {
            SolutionPrinter.printSolution(s, sol);
        });

        // Display search statistics
        System.out.println(stats);
    }
}

