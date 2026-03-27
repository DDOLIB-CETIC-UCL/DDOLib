package org.ddolib.examples.misp;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.common.solver.Solution;
import org.ddolib.modeling.LnsModel;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solvers;
import org.ddolib.util.io.SolutionPrinter;

import java.io.IOException;
import java.nio.file.Path;
import java.util.BitSet;

/**
 * Entry point for solving the Maximum Independent Set Problem (MISP)
 * using a Large Neighborhood Search (LNS) approach combined with
 * Decision Diagram Optimization (DDO).
 *
 * <p>
 * The Maximum Independent Set problem consists in selecting a largest possible
 * subset of vertices in a graph such that no two selected vertices are adjacent.
 * In other words, the selected vertices form an independent set.
 * </p>
 *
 * <p>
 * This class demonstrates how to:
 * </p>
 * <ul>
 *     <li>Load a graph instance (in DOT format)</li>
 *     <li>Define a {@code LnsModel} with problem-specific components</li>
 *     <li>Use a lower bound heuristic to guide the search</li>
 *     <li>Apply dominance rules to prune suboptimal states</li>
 *     <li>Run a Large Neighborhood Search (LNS) optimization</li>
 *     <li>Print intermediate and final solutions</li>
 * </ul>
 *
 *
 * <h2>Execution</h2>
 * <p>
 * The program accepts an optional command-line argument specifying
 * the path to a MISP instance file. If not provided, a default
 * instance is loaded from:
 * </p>
 * <pre>
 * data/MISP/tadpole_4_2.dot
 * </pre>
 *
 *
 * <h2>Model Components</h2>
 * <ul>
 *     <li>{@link MispProblem} – defines the graph structure</li>
 *     <li>{@link MispFastLowerBound} – provides a fast lower bound on the independent set size</li>
 *     <li>{@link MispDominance} – defines dominance relations between states</li>
 *     <li>{@link SimpleDominanceChecker} – applies dominance pruning</li>
 *     <li>{@link MispRanking} – ranks states during decision diagram compilation</li>
 * </ul>
 *
 * <h2>Search Configuration</h2>
 * <ul>
 *     <li>Search strategy: Large Neighborhood Search (LNS)</li>
 *     <li>Time limit: 100 milliseconds</li>
 * </ul>
 *
 * <p>
 * No width heuristic is explicitly specified, so default behavior
 * (if provided by the framework) is used.
 * </p>
 *
 * <h2>Output</h2>
 * <p>
 * The program prints:
 * </p>
 * <ul>
 *     <li>Intermediate solutions during the search</li>
 *     <li>Final solution statistics</li>
 *     <li>The best independent set found</li>
 * </ul>
 *
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

    /**
     * Main entry point of the program.
     *
     * <p>
     * Loads a Maximum Independent Set instance, configures the LNS model
     * with problem-specific heuristics and dominance rules, and runs
     * the optimization process.
     * </p>
     *
     * @param args optional command-line arguments:
     *             <ul>
     *                 <li>{@code args[0]} – path to the MISP instance file</li>
     *             </ul>
     * @throws IOException if the instance file cannot be read
     */
    public static void main(String[] args) throws IOException {

        final String instance = args.length == 0
                ? Path.of("data", "MISP", "tadpole_4_2.dot").toString()
                : args[0];

        final MispProblem problem = new MispProblem(instance);

        LnsModel<BitSet> model = new LnsModel<>() {
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
                return new SimpleDominanceChecker<>(
                        new MispDominance(),
                        problem.nbVars()
                );
            }

            @Override
            public MispRanking ranking() {
                return new MispRanking();
            }
        };

        Solution bestSolution = Solvers.minimizeLns(
                model,
                s -> s.runTimeMs() < 100,
                (sol, s) -> {
                    SolutionPrinter.printSolution(s, sol);
                }
        );

        System.out.println(bestSolution.statistics());
        System.out.println(bestSolution);
    }
}