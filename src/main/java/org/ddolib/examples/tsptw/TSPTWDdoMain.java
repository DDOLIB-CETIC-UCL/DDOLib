package org.ddolib.examples.tsptw;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.Frontier;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.modeling.DdoModel;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solvers;
import org.ddolib.util.io.SolutionPrinter;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * The Traveling Salesman Problem with Time Windows (TSP with Time Windows) with Ddo.
 * Main class for solving the Traveling Salesman Problem with Time Windows (TSPTW)
 * using a Decision Diagram Optimization (DDO) approach.
 * <p>
 * This class demonstrates how to load a TSPTW problem instance, define a DDO model with relaxation, ranking,
 * lower bound, dominance, frontier, width heuristic, and caching, and solve the problem using
 * {@link Solvers#minimizeDdo(DdoModel, java.util.function.BiConsumer)}.
 * The solution and search statistics are printed to the console.
 * </p>
 * <p>
 * Default data files come from
 * <a href="https://lopez-ibanez.eu/tsptw-instances#makespan">López-Ibáñes and Blum benchmark instances</a>.
 * </p>
 */
public class TSPTWDdoMain {

    /**
     * Entry point of the application.
     * <p>
     * Execution examples:
     * </p>
     * <ul>
     *     <li>Run the default instance:
     *     {@code mvn exec:java -Dexec.mainClass="org.ddolib.ddosolver.examples.tsptw.TSPTWMain"}</li>
     *     <li>Run a specific instance with optional maximum MDD width:
     *     {@code mvn exec:java -Dexec.mainClass="org.ddolib.ddosolver.examples.tsptw.TSPTWMain" -Dexec.args="<your file> <max width>"}</li>
     * </ul>
     *
     * <p>
     * The method performs the following steps:
     * </p>
     * <ol>
     *     <li>Loads the TSPTW problem instance from a file (default or provided via arguments).</li>
     *     <li>Defines a DDO model including:
     *         <ul>
     *             <li>Relaxation using {@link TSPTWRelax}</li>
     *             <li>Ranking using {@link TSPTWRanking}</li>
     *             <li>Lower bound using {@link TSPTWFastLowerBound}</li>
     *             <li>Dominance using {@link TSPTWDominance} and {@link SimpleDominanceChecker}</li>
     *             <li>Frontier using {@link SimpleFrontier} and {@link CutSetType#Frontier}</li>
     *             <li>Width heuristic using {@link FixedWidth}</li>
     *             <li>Caching enabled</li>
     *         </ul>
     *     </li>
     *     <li>Solves the problem using the DDO solver.</li>
     *     <li>Prints the solution and search statistics to the console.</li>
     * </ol>
     *
     * @param args optional command-line arguments specifying the instance file and maximum width
     * @throws IOException if an I/O error occurs while reading the instance file or printing the solution
     */
    public static void main(String[] args) throws IOException {
        final String instance = args.length == 0 ?
                Path.of("data", "TSPTW", "nbNodes_4_1.txt").toString() : args[0];
        final TSPTWProblem problem = new TSPTWProblem(instance);
        DdoModel<TSPTWState> model = new DdoModel<>() {
            @Override
            public Problem<TSPTWState> problem() {
                return problem;
            }

            @Override
            public TSPTWRelax relaxation() {
                return new TSPTWRelax(problem);
            }

            @Override
            public TSPTWRanking ranking() {
                return new TSPTWRanking();
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
            public Frontier<TSPTWState> frontier() {
                return new SimpleFrontier<>(ranking(), CutSetType.Frontier);
            }

           /* @Override
            public boolean useCache() {
                return true;
            }*/

            @Override
            public WidthHeuristic<TSPTWState> widthHeuristic() {
                return new FixedWidth<>(20);
            }

        };

        int[] bestSol = new int[problem.nbVars()];

        SearchStatistics stats = Solvers.minimizeDdo(model, (sol, s) -> {
            SolutionPrinter.printSolution(s, sol);
            System.out.println("Optimal solution found");
            Arrays.setAll(bestSol, i -> sol[i]);
        });

        System.out.println(stats);
        String str = "0 -> " +
                Arrays.stream(bestSol).mapToObj(Objects::toString).collect(Collectors.joining(" " +
                        "-> "));

        System.out.println(str);
    }
}
