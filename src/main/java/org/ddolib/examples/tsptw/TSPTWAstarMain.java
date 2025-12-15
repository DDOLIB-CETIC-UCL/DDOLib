package org.ddolib.examples.tsptw;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.common.solver.Solution;
import org.ddolib.modeling.Model;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solvers;
import org.ddolib.util.io.SolutionPrinter;

import java.io.IOException;
import java.nio.file.Path;

/**
 * The Traveling Salesman Problem with Time Windows (TSP with Time Windows) with AsTar.
 * Main class to solve the Traveling Salesman Problem with Time Windows (TSPTW)
 * using the A* search algorithm.
 *
 * <p>
 * This class initializes a {@link TSPTWProblem} instance, sets up an A* model,
 * and runs the A* solver to find an optimal or near-optimal solution to the TSPTW.
 * </p>
 *
 * <p>
 * <b>Usage:</b>
 * </p>
 * <ul>
 *     <li>Run the default instance from the command line using Maven:
 *     <pre>
 * mvn exec:java -Dexec.mainClass="org.ddolib.ddosolver.examples.tsptw.TSPTWAstarMain"
 *     </pre>
 *     </li>
 *     <li>Specify a custom instance file and optionally the maximum MDD width:
 *     <pre>
 * mvn exec:java -Dexec.mainClass="org.ddolib.ddosolver.examples.tsptw.TSPTWAstarMain" -Dexec.args="&lt;your file&gt; &lt;max MDD width&gt;"
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
 * @see Model
 * @see Solvers
 */
public class TSPTWAstarMain {

    /**
     * Entry point for running the A* solver on a TSPTW instance.
     *
     * @param args Optional command-line arguments:
     *             the first argument can be the path to a TSPTW instance file.
     * @throws IOException If there is an error reading the input instance file.
     */
    public static void main(String[] args) throws IOException {
        final String instance = args.length == 0 ? Path.of("data", "TSPTW", "AFG", "rbg010a.tw").toString() : args[0];
        final TSPTWProblem problem = new TSPTWProblem(instance);
        Model<TSPTWState> model = new Model<>() {
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

        Solution bestSolution = Solvers.minimizeAstar(model, (sol, s) -> {
            SolutionPrinter.printSolution(s, sol);
        });

        System.out.println(bestSolution.statistics());
        System.out.println(bestSolution);
    }
}
