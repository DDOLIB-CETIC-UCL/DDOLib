package org.ddolib.examples.tsptw;

import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.common.solver.Solver;
import org.ddolib.common.solver.SolverConfig;
import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.cache.SimpleCache;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.profiling.SearchStatistics;
import org.ddolib.ddo.core.solver.ExactSolver;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The TSPTW (TSP with Time Windows) is
 * to find the shortest possible route for a salesman to visit
 * a set of customers (or nodes) exactly once
 * and return to the starting point, while respecting
 * specified time windows for each customer.
 */
public class TSPTWMain {

    /**
     * Run {@code mvn exec:java -Dexec.mainClass="org.ddolib.ddosolver.examples.tsptw.TSPTWMain"} in your terminal to execute
     * default instance. <br>
     * <p>
     * Run {@code mvn exec:java -Dexec.mainClass="org.ddolib.ddosolver.examples.tsptw.TSPTWMain -Dexec.args="<your file>
     * <maximum width of the mdd>"} to specify an instance and optionally the maximum width of the mdd.<br>
     * <p>
     * Given Data files comes from
     * <a href="https://lopez-ibanez.eu/tsptw-instances#makespan">López-Ibáñes and Blum benchmark instances</a>.
     */
    public static void main(String[] args) throws IOException {

        final String file = args.length == 0 ?
                Paths.get("data", "TSPTW", "nbNodes_4_1.txt").toString() : args[0];

        SolverConfig<TSPTWState, TSPTWDominanceKey> config = new SolverConfig<>();
        final TSPTWProblem problem = new TSPTWProblem(new TSPTWInstance(file));
        config.problem = problem;
        config.relax = new TSPTWRelax(problem);
        config.ranking = new TSPTWRanking();
        config.fub = new TSPTWFastUpperBound(problem);

        config.width = new FixedWidth<>(2);
        config.varh = new DefaultVariableHeuristic<>();
        config.dominance = new SimpleDominanceChecker<>(new TSPTWDominance(), problem.nbVars());
        config.frontier = new SimpleFrontier<>(config.ranking, CutSetType.LastExactLayer);
        config.exportAsDot = true;
        config.cache = new SimpleCache<>();


        final Solver solver = new ExactSolver<>(config);

        long start = System.currentTimeMillis();
        SearchStatistics stat = solver.maximize();
        double duration = (System.currentTimeMillis() - start) / 1000.0;


        Optional<Set<Decision>> bestSol = solver.bestSolution();

        String solutionStr;
        if (bestSol.isPresent()) {
            int[] solution = solver.constructBestSolution(problem.nbVars());
            solutionStr = "0 -> " + Arrays.stream(solution)
                    .mapToObj(String::valueOf)
                    .collect(Collectors.joining(" -> "));
        } else {
            solutionStr = "No feasible solution";
        }

        String bestStr = solver.bestValue().map(Object::toString).orElse("No feasible solution");

        System.out.printf("Instance : %s%n", file);
        System.out.printf("Duration : %.3f seconds%n", duration);
        System.out.printf("Objective: %s%n", bestStr);
        System.out.printf("Solution : %s%n", solutionStr);
        System.out.println(stat);
    }
}
