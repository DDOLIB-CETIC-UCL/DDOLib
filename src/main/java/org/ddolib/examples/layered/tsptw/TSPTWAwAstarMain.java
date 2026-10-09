package org.ddolib.examples.layered.tsptw;

import static org.ddolib.common.solver.stopcriterion.AstarStopCriterion.minValidChildrenPercent;
import static org.ddolib.common.solver.stopcriterion.StopCriterion.maxIterSinceLastImprovement;

import java.io.IOException;
import java.nio.file.Path;
import org.ddolib.common.util.io.SolutionPrinter;
import org.ddolib.layered.modeling.AwAstarModel;
import org.ddolib.layered.modeling.DominanceChecker;
import org.ddolib.layered.modeling.Model;
import org.ddolib.layered.modeling.Problem;
import org.ddolib.layered.modeling.SimpleDominanceChecker;
import org.ddolib.layered.modeling.Solvers;
import org.ddolib.layered.solver.Solution;

/**
 * The Traveling Salesman Problem with Time Windows (TSP with Time Windows) with Anytime Weighted A*
 * (AWA*). Main class to solve the Traveling Salesman Problem with Time Windows (TSPTW) using the
 * AWA* search algorithm.
 *
 * <p>This class initializes a {@link TSPTWProblem} instance, sets up an A* model, and runs the AWA*
 * solver to find an optimal or near-optimal solution to the TSPTW.
 *
 * <p><b>Usage:</b>
 *
 * <ul>
 *   <li>Run the default instance from the command line using Maven:
 *       <pre>
 * mvn exec:java -Dexec.mainClass="org.ddolib.examples.layered.tsptw.TSPTWAwAstarMain"
 *     </pre>
 *   <li>Specify a custom instance file and optionally the maximum MDD width:
 *       <pre>
 * mvn exec:java -Dexec.mainClass="org.ddolib.examples.layered.tsptw.TSPTWAwAstarMain" \
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
 * @see Model
 * @see Solvers
 */
public class TSPTWAwAstarMain {

    private TSPTWAwAstarMain() {}

    /**
     * Entry point for running the A* solver on a TSPTW instance.
     *
     * @param args optional command-line arguments: the first argument can be the path to a TSPTW
     *     instance file.
     * @throws IOException if there is an error reading the input instance file
     */
    public static void main(String[] args) throws IOException {
        final String instance =
                args.length == 0
                        ? Path.of("data", "TSPTW", "AFG", "rbg172a.tw").toString()
                        : args[0];
        final TSPTWProblem problem = new TSPTWProblem(instance);

        AwAstarModel<TSPTWState> model =
                new AwAstarModel<>() {
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

                    @Override
                    public double weight() {
                        return 7.5;
                    }
                };

        var stop = maxIterSinceLastImprovement(200).and(minValidChildrenPercent(11));
        Solution bestSolution =
                Solvers.minimizeAwAStar(
                        model,
                        stop,
                        (sol, s) -> {
                            SolutionPrinter.printSolution(s, sol);
                        });

        System.out.println(bestSolution.statistics());
        System.out.println(bestSolution);
    }
}
