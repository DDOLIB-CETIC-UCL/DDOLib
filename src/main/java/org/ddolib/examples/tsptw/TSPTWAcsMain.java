package org.ddolib.examples.tsptw;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.modeling.AcsModel;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solvers;
import org.ddolib.util.io.SolutionPrinter;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * The Traveling Salesman Problem with Time Windows (TSP with Time Windows) with Acs.
 * Main class to solve the Traveling Salesman Problem with Time Windows (TSPTW)
 * using the Anytime column Search (ACS) algorithm.
 *
 * <p>
 * This class sets up a {@link TSPTWProblem} instance, initializes an ACS model,
 * and runs the ACS solver to find an approximate solution to the TSPTW.
 * </p>
 *
 * <p>
 * <b>Usage:</b>
 * </p>
 * <ul>
 *     <li>Run the default instance from the command line using Maven:
 *     <pre>
 * mvn exec:java -Dexec.mainClass="org.ddolib.ddosolver.examples.tsptw.TSPTWAcsMain"
 *     </pre>
 *     </li>
 *     <li>Specify a custom instance file and optionally the maximum MDD width:
 *     <pre>
 * mvn exec:java -Dexec.mainClass="org.ddolib.ddosolver.examples.tsptw.TSPTWAcsMain" -Dexec.args="&lt;your file&gt; &lt;max MDD width&gt;"
 *     </pre>
 *     </li>
 * </ul>
 *
 * <p>
 * Default benchmark instances are taken from
 * <a href="https://lopez-ibanez.eu/tsptw-instances#makespan">López-Ibáñes and Blum TSPTW instances</a>.
 * </p>
 *
 * @see TSPTWProblem
 * @see TSPTWState
 * @see TSPTWFastLowerBound
 * @see AcsModel
 * @see Solvers
 */
public class TSPTWAcsMain {

    /**
     * Entry point for running the ACS solver on a TSPTW instance.
     *
     * @param args Optional command-line arguments:
     *             the first argument can be the path to a TSPTW instance file.
     * @throws IOException If there is an error reading the input instance file.
     */
    public static void main(String[] args) throws IOException {
        final String instance = args.length == 0 ? Path.of("data", "TSPTW", "AFG", "rbg010a.tw").toString() : args[0];
        final TSPTWProblem problem = new TSPTWProblem(instance);
        AcsModel<TSPTWState> model = new AcsModel<>() {
            @Override
            public Problem<TSPTWState> problem() {
               return problem;
            }

            @Override
            public TSPTWFastLowerBound lowerBound() {
                return new TSPTWFastLowerBound(problem);
            }

            @Override
            public DominanceChecker<TSPTWState> dominance() {
                return new SimpleDominanceChecker<>(new TSPTWDominance(), problem.nbVars());
            }
        };

        SearchStatistics stats = Solvers.minimizeAcs(model, (sol, s) -> {
            SolutionPrinter.printSolution(s,sol);
        });
        System.out.println(stats);
    }
}
