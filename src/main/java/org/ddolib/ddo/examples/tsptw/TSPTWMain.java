package org.ddolib.ddo.examples.tsptw;

import org.ddolib.ddo.core.*;
import org.ddolib.ddo.heuristics.VariableHeuristic;
import org.ddolib.ddo.implem.dominance.SimpleDominanceChecker;
import org.ddolib.ddo.implem.frontier.SimpleFrontier;
import org.ddolib.ddo.implem.heuristics.DefaultVariableHeuristic;
import org.ddolib.ddo.implem.heuristics.FixedWidth;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.ddolib.ddo.implem.solver.Solvers.sequentialSolver;

/**
 * The TSPTW (TSP with Time Windows) is
 * to find the shortest possible route for a salesman to visit
 * a set of customers (or nodes) exactly once
 * and return to the starting point, while respecting
 * specified time windows for each customer.
 */
public class TSPTWMain {

    /**
     * Run {@code mvn exec:java -Dexec.mainClass="org.ddolib.ddo.examples.tsptw.TSPTWMain"} in your terminal to execute
     * default instance. <br>
     * <p>
     * Run {@code mvn exec:java -Dexec.mainClass="org.ddolib.ddo.examples.tsptw.TSPTWMain -Dexec.args="<your file>
     * <maximum width of the mdd>"} to specify an instance and optionally the maximum width of the mdd.<br>
     * <p>
     * Given Data files comes from
     * <a href="https://lopez-ibanez.eu/tsptw-instances#makespan">López-Ibáñes and Blum benchmark instances</a>.
     */
    public static void main(String[] args) throws IOException {

        final String file = args.length == 0 ? Paths.get("data", "TSPTW", "AFG", "rbg020a.tw").toString() : args[0];
        final int widthFactor = args.length >= 2 ? Integer.parseInt(args[1]) : 50;
        final TSPTWProblem problem = new TSPTWProblem(new TSPTWInstance(file));

        final TSPTWRelax relax = new TSPTWRelax(problem);
        final TSPTWRanking ranking = new TSPTWRanking();

        final FixedWidth<TSPTWState> width = new FixedWidth<>(20);
        final VariableHeuristic<TSPTWState> varh = new DefaultVariableHeuristic<>();
        final SimpleDominanceChecker<TSPTWState, TSPTWDominanceKey> dominance =
                new SimpleDominanceChecker<>(new TSPTWDominance(), problem.nbVars());
        final Frontier<TSPTWState> frontier = new SimpleFrontier<>(ranking, CutSetType.LastExactLayer);


        final Solver solver = sequentialSolver(
                problem,
                relax,
                varh,
                ranking,
                width,
                frontier,
                dominance
        );

        long start = System.currentTimeMillis();
        SearchStatistics stat = solver.maximize();
        double duration = (System.currentTimeMillis() - start) / 1000.0;


        Optional<Set<Decision>> bestSol = solver.bestSolution();

        String solutionStr;
        if (bestSol.isPresent()) {
            int[] solution = bestSol.map(decisions -> {
                int[] values = new int[problem.nbVars()];
                for (Decision d : decisions) {
                    values[d.var()] = d.val();
                }
                return values;
            }).get();
            solutionStr = "0 -> " + Arrays.stream(solution)
                    .mapToObj(String::valueOf)
                    .collect(Collectors.joining(" -> "));
        } else {
            solutionStr = "No feasible solution";
        }

        String bestStr = solver.bestValue().isPresent() ? "" + solver.bestValue().get() : "No value";


        System.out.printf("Instance : %s%n", file);
        System.out.printf("Width factor : %d%n", widthFactor);
        System.out.printf("Duration : %.3f seconds%n", duration);
        System.out.printf("Objective: %s%n", bestStr);
        System.out.printf("Solution : %s%n", solutionStr);
        System.out.println(stat);
    }
}
