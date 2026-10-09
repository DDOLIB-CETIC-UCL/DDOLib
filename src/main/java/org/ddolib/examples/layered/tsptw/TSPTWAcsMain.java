package org.ddolib.examples.layered.tsptw;

import java.io.IOException;
import java.nio.file.Path;
import org.ddolib.common.util.io.SolutionPrinter;
import org.ddolib.layered.modeling.AcsModel;
import org.ddolib.layered.modeling.DominanceChecker;
import org.ddolib.layered.modeling.Problem;
import org.ddolib.layered.modeling.SimpleDominanceChecker;
import org.ddolib.layered.modeling.Solvers;
import org.ddolib.layered.solver.Solution;

/**
 * The Traveling Salesman Problem with Time Windows (TSP with Time Windows) with Acs. Main class to
 * solve the Traveling Salesman Problem with Time Windows (TSPTW) using the Anytime column Search
 * (ACS) algorithm.
 *
 * <p>This class sets up a {@link TSPTWProblem} instance, initializes an ACS model, and runs the ACS
 * solver to find an approximate solution to the TSPTW.
 *
 * <p><b>Usage:</b>
 *
 * <ul>
 *   <li>Run the default instance from the command line using Maven:
 *       <pre>
 * mvn exec:java -Dexec.mainClass="org.ddolib.examples.layered.tsptw.TSPTWAcsMain"
 *     </pre>
 *   <li>Specify a custom instance file and optionally the maximum MDD width:
 *       <pre>
 * mvn exec:java -Dexec.mainClass="org.ddolib.examples.layered.tsptw.TSPTWAcsMain" \
 *     -Dexec.args="&lt;your file&gt; &lt;max MDD width&gt;"
 *     </pre>
 * </ul>
 *
 * <p>Default benchmark instances are taken from <a
 * href="https://lopez-ibanez.eu/tsptw-instances#makespan">López-Ibáñes and Blum TSPTW
 * instances</a>.
 *
 * @see TSPTWProblem
 * @see TSPTWState
 * @see TSPTWFastLowerBound
 * @see AcsModel
 * @see Solvers
 */
public class TSPTWAcsMain {

    private TSPTWAcsMain() {}

    /**
     * Entry point for running the ACS solver on a TSPTW instance.
     *
     * @param args optional command-line arguments: the first argument can be the path to a TSPTW
     *     instance file.
     * @throws IOException if there is an error reading the input instance file
     */
    public static void main(String[] args) throws IOException {
        final String instance =
                args.length == 0
                        ? Path.of("data", "TSPTW", "AFG", "rbg010a.tw").toString()
                        : args[0];
        final TSPTWProblem problem = new TSPTWProblem(instance);
        AcsModel<TSPTWState> model =
                new AcsModel<>() {
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

        Solution bestSolution =
                Solvers.minimizeAcs(
                        model,
                        (sol, s) -> {
                            SolutionPrinter.printSolution(s, sol);
                        });
        System.out.println(bestSolution.statistics());
        System.out.println(bestSolution);
    }
}
