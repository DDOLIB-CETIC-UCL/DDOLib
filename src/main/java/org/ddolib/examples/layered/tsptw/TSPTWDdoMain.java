package org.ddolib.examples.layered.tsptw;

import java.io.IOException;
import java.nio.file.Path;
import org.ddolib.common.frontier.CutSetType;
import org.ddolib.common.frontier.Frontier;
import org.ddolib.common.frontier.SimpleFrontier;
import org.ddolib.common.heuristics.width.FixedWidth;
import org.ddolib.common.heuristics.width.WidthHeuristic;
import org.ddolib.common.util.io.SolutionPrinter;
import org.ddolib.layered.modeling.DdoModel;
import org.ddolib.layered.modeling.DominanceChecker;
import org.ddolib.layered.modeling.Problem;
import org.ddolib.layered.modeling.SimpleDominanceChecker;
import org.ddolib.layered.modeling.Solvers;
import org.ddolib.layered.solver.Solution;

/**
 * The Traveling Salesman Problem with Time Windows (TSP with Time Windows) with Ddo. Main class for
 * solving the Traveling Salesman Problem with Time Windows (TSPTW) using a Decision Diagram
 * Optimization (DDO) approach.
 *
 * <p>This class demonstrates how to load a TSPTW problem instance, define a DDO model with
 * relaxation, ranking, lower bound, dominance, frontier, width heuristic, and caching, and solve
 * the problem using {@link Solvers#minimizeDdo(DdoModel, java.util.function.BiConsumer)}. The
 * solution and search statistics are printed to the console.
 *
 * <p>Default data files come from <a
 * href="https://lopez-ibanez.eu/tsptw-instances#makespan">López-Ibáñes and Blum benchmark
 * instances</a>.
 */
public class TSPTWDdoMain {

    private TSPTWDdoMain() {}

    /**
     * Entry point of the application.
     *
     * <p>Execution examples:
     *
     * <ul>
     *   <li>Run the default instance: {@code mvn exec:java
     *       -Dexec.mainClass="org.ddolib.ddosolver.examples.tsptw.TSPTWMain"}
     *   <li>Run a specific instance with optional maximum MDD width: {@code mvn exec:java
     *       -Dexec.mainClass="org.ddolib.ddosolver.examples.tsptw.TSPTWMain" -Dexec.args="<your
     *       file> <max width>"}
     * </ul>
     *
     * <p>The method performs the following steps:
     *
     * <ol>
     *   <li>Loads the TSPTW problem instance from a file (default or provided via arguments).
     *   <li>Defines a DDO model including:
     *       <ul>
     *         <li>Relaxation using {@link TSPTWRelax}
     *         <li>Ranking using {@link TSPTWRanking}
     *         <li>Lower bound using {@link TSPTWFastLowerBound}
     *         <li>Dominance using {@link TSPTWDominance} and {@link SimpleDominanceChecker}
     *         <li>Frontier using {@link SimpleFrontier} and {@link CutSetType#Frontier}
     *         <li>Width heuristic using {@link FixedWidth}
     *         <li>Caching enabled
     *       </ul>
     *   <li>Solves the problem using the DDO solver.
     *   <li>Prints the solution and search statistics to the console.
     * </ol>
     *
     * @param args optional command-line arguments specifying the instance file and maximum width
     * @throws IOException if an I/O error occurs while reading the instance file or printing the
     *     solution
     */
    public static void main(String[] args) throws IOException {
        String instance =
                args.length == 0
                        ? Path.of("data", "TSPTW", "AFG", "rbg010a.tw").toString()
                        : args[0];
        final TSPTWProblem problem = new TSPTWProblem(instance);
        DdoModel<TSPTWState> model =
                new DdoModel<>() {
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
                    public TSPTWRelax relaxation() {
                        return new TSPTWRelax(problem);
                    }

                    @Override
                    public TSPTWRanking ranking() {
                        return new TSPTWRanking();
                    }

                    @Override
                    public WidthHeuristic<TSPTWState> widthHeuristic() {
                        return new FixedWidth<>(2);
                    }

                    @Override
                    public Frontier<TSPTWState> frontier() {
                        return new SimpleFrontier<>(ranking(), CutSetType.Frontier);
                    }

                    @Override
                    public boolean useCache() {
                        return true;
                    }
                };

        Solution bestSolution =
                Solvers.minimizeDdo(
                        model,
                        (sol, s) -> {
                            SolutionPrinter.printSolution(s, sol);
                        });

        System.out.println(bestSolution.statistics());
        System.out.println(bestSolution);
    }
}
