package org.ddolib.examples.layered.misp;

import java.io.IOException;
import java.nio.file.Path;
import java.util.BitSet;
import org.ddolib.common.util.io.SolutionPrinter;
import org.ddolib.layered.modeling.DominanceChecker;
import org.ddolib.layered.modeling.LnsModel;
import org.ddolib.layered.modeling.Problem;
import org.ddolib.layered.modeling.SimpleDominanceChecker;
import org.ddolib.layered.modeling.Solvers;
import org.ddolib.layered.solver.Solution;

/**
 * Entry point for solving the Maximum Independent Set Problem (MISP) using a Large Neighborhood
 * Search (LNS) approach combined with Decision Diagram Optimization (DDO).
 *
 * <p>The Maximum Independent Set problem consists in selecting a largest possible subset of
 * vertices in a graph such that no two selected vertices are adjacent. In other words, the selected
 * vertices form an independent set.
 *
 * <p>This class demonstrates how to:
 *
 * <ul>
 *   <li>Load a graph instance (in DOT format)
 *   <li>Define a {@code LnsModel} with problem-specific components
 *   <li>Use a lower bound heuristic to guide the search
 *   <li>Apply dominance rules to prune suboptimal states
 *   <li>Run a Large Neighborhood Search (LNS) optimization
 *   <li>Print intermediate and final solutions
 * </ul>
 *
 * <h2>Execution</h2>
 *
 * <p>The program accepts an optional command-line argument specifying the path to a MISP instance
 * file. If not provided, a default instance is loaded from:
 *
 * <pre>
 * data/MISP/tadpole_4_2.dot
 * </pre>
 *
 * <h2>Model Components</h2>
 *
 * <ul>
 *   <li>{@link MispProblem} – defines the graph structure
 *   <li>{@link MispFastLowerBound} – provides a fast lower bound on the independent set size
 *   <li>{@link MispDominance} – defines dominance relations between states
 *   <li>{@link SimpleDominanceChecker} – applies dominance pruning
 *   <li>{@link MispRanking} – ranks states during decision diagram compilation
 * </ul>
 *
 * <h2>Search Configuration</h2>
 *
 * <ul>
 *   <li>Search strategy: Large Neighborhood Search (LNS)
 *   <li>Time limit: 100 milliseconds
 * </ul>
 *
 * <p>No width heuristic is explicitly specified, so default behavior (if provided by the framework)
 * is used.
 *
 * <h2>Output</h2>
 *
 * <p>The program prints:
 *
 * <ul>
 *   <li>Intermediate solutions during the search
 *   <li>Final solution statistics
 *   <li>The best independent set found
 * </ul>
 *
 * @see MispProblem
 * @see MispFastLowerBound
 * @see MispDominance
 * @see MispRanking
 * @see LnsModel
 * @see Solvers#minimizeLns
 * @see Solution
 */
public class MispLnsMain {

    private MispLnsMain() {}

    /**
     * Main entry point of the program.
     *
     * <p>Loads a Maximum Independent Set instance, configures the LNS model with problem-specific
     * heuristics and dominance rules, and runs the optimization process.
     *
     * @param args optional command-line arguments:
     *     <ul>
     *       <li>{@code args[0]} – path to the MISP instance file
     *     </ul>
     *
     * @throws IOException if the instance file cannot be read
     */
    public static void main(String[] args) throws IOException {

        final String instance =
                args.length == 0 ? Path.of("data", "MISP", "tadpole_4_2.dot").toString() : args[0];

        final MispProblem problem = new MispProblem(instance);

        LnsModel<BitSet> model =
                new LnsModel<>() {
                    @Override
                    public Problem<BitSet> problem() {
                        return problem;
                    }

                    @Override
                    public MispFastLowerBound lowerBound() {
                        return new MispFastLowerBound(problem);
                    }

                    @Override
                    public DominanceChecker<BitSet> dominance() {
                        return new SimpleDominanceChecker<>(new MispDominance(), problem.nbVars());
                    }

                    @Override
                    public MispRanking ranking() {
                        return new MispRanking();
                    }
                };

        Solution bestSolution =
                Solvers.minimizeLns(
                        model,
                        s -> s.runtime() > 1000,
                        (sol, s) -> {
                            SolutionPrinter.printSolution(s, sol);
                        });

        System.out.println(bestSolution.statistics());
        System.out.println(bestSolution);
    }
}
