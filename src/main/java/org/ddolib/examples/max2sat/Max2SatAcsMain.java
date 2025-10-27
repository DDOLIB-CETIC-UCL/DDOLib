package org.ddolib.examples.max2sat;

import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.modeling.AcsModel;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solvers;
import org.ddolib.util.io.SolutionPrinter;

import java.io.IOException;
import java.nio.file.Path;
/**
 * Maximum 2-Satisfiability (MAX2SAT) (MAX2SAT) problem with Acs.
 * Entry point for solving the <b>Maximum 2-Satisfiability (MAX2SAT)</b> problem
 * using the <b>Anytime Column Search (ACS)</b> algorithm.
 *
 * <p>
 * This class demonstrates how to set up and run an ACS-based solver using
 * the {@link Max2SatProblem}, {@link Max2SatState}, and {@link Max2SatFastLowerBound}
 * components. The Ant Colony System is a stochastic optimization algorithm
 * inspired by the foraging behavior of ants, which iteratively refines
 * candidate solutions by simulating pheromone updates and heuristic choices.
 * </p>
 *
 * <p><b>Usage:</b></p>
 * <ul>
 *     <li>If no command-line argument is provided, the default instance file
 *     <code>data/Max2Sat/wcnf_var_4_opti_39.txt</code> is used.</li>
 *     <li>Otherwise, the first argument should specify the path to a weighted CNF instance file
 *     (in WCNF format).</li>
 * </ul>
 *
 * <p><b>Example:</b></p>
 * <pre>{@code
 * java Max2SatAcsMain data/Max2Sat/wcnf_var_20_opti_110.txt
 * }</pre>
 *
 * <p>
 * The program prints the best solution found (assignment and cost) as well as
 * search statistics (iterations, best cost, time, etc.).
 * </p>
 *
 * @see Max2SatProblem
 * @see Max2SatState
 * @see Max2SatFastLowerBound
 * @see AcsModel
 * @see Solvers
 * @see SearchStatistics
 */
public final class Max2SatAcsMain {
    /**
     * Main entry point for the MAX2SAT Ant Colony System solver.
     *
     * @param args an optional array containing the path to a WCNF instance file;
     *             if empty, a default test instance is used.
     * @throws IOException if the instance file cannot be read or parsed.
     */
    public static void main(String[] args) throws IOException {
        String instance = args.length == 0 ? Path.of("data", "Max2Sat", "wcnf_var_4_opti_39.txt").toString() : args[0];
        final Max2SatProblem problem = new Max2SatProblem(instance);
        AcsModel<Max2SatState> model = new AcsModel<>() {
            @Override
            public Problem<Max2SatState> problem() {
                return problem;
            }

            @Override
            public Max2SatFastLowerBound lowerBound() {
                return new Max2SatFastLowerBound(problem);
            }
        };

        SearchStatistics stats = Solvers.minimizeAcs(model, (sol,s) -> {;
            SolutionPrinter.printSolution(s,sol);
        });

        System.out.println(stats);

    }
}
