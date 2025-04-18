package org.ddolib.ddo.examples.tsptw;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Frontier;
import org.ddolib.ddo.core.SearchStatistics;
import org.ddolib.ddo.heuristics.VariableHeuristic;
import org.ddolib.ddo.implem.frontier.SimpleFrontier;
import org.ddolib.ddo.implem.heuristics.DefaultVariableHeuristic;
import org.ddolib.ddo.implem.solver.ParallelSolver;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

public class TSPTW {


    /**
     * Creates instance from data files.<br>
     * <p>
     * The expected format is the following:
     * <ul>
     *     <li>
     *         The first line must contain the number of variable. A second  optional value can be
     *         given: the expected objective value for an optimal solution.
     *     </li>
     *     <li>
     *         The time matrix.
     *     </li>
     *     <li>
     *         A time window for each node.
     *     </li>
     * </ul>
     *
     * @param fileName The path to the input file.
     * @return An instance of TSPTWProblem
     * @throws IOException If something goes wrong while reading input file.
     */
    public static TSPTWProblem readInstance(String fileName) throws IOException {
        int numVar = 0;
        int[][] distance = new int[0][0];
        TimeWindow[] timeWindows = new TimeWindow[0];
        Optional<Integer> optimal = Optional.empty();

        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            int lineCount = 0;

            String line;

            while ((line = br.readLine()) != null) {
                //Skip comment
                if (line.startsWith("#") || line.isEmpty()) {
                    continue;
                }

                if (lineCount == 0) {
                    String[] tokens = line.split("\\s+");
                    numVar = Integer.parseInt(tokens[0]);
                    distance = new int[numVar][numVar];
                    timeWindows = new TimeWindow[numVar];
                    if (tokens.length == 2) optimal = Optional.of(Integer.parseInt(tokens[1]));
                } else if (1 <= lineCount && lineCount <= numVar) {
                    int i = lineCount - 1;
                    String[] distanceFromI = line.split("\\s+");
                    distance[i] = Arrays.stream(distanceFromI).mapToInt(Integer::parseInt).toArray();
                } else {
                    int i = lineCount - 1 - numVar;
                    String[] tw = line.split("\\s+");
                    timeWindows[i] = new TimeWindow(Integer.parseInt(tw[0]), Integer.parseInt(tw[1]));
                }
                lineCount++;
            }
            return new TSPTWProblem(distance, timeWindows, optimal);
        }
    }

    /**
     * Run {@code mvn exec:java -Dexec.mainClass="org.ddolib.ddo.examples.tsptw.TSPTW"} in your terminal to execute
     * default instance. <br>
     * <p>
     * Run {@code mvn exec:java -Dexec.mainClass="org.ddolib.ddo.examples.tsptw.TSPTW -Dexec.args="<your file> <maximum
     * width of the mdd>"} to specify an instance and optionally the maximum width of the mdd.<br>
     * <p>
     * Given Data files comes from
     * <a href="https://lopez-ibanez.eu/tsptw-instances#makespan">López-Ibáñes and Blum benchmark instances</a>.
     */
    public static void main(String[] args) throws IOException {
        final String file = args.length == 0 ? Paths.get("data", "TSPTW", "AFG", "rbg020a.tw").toString() : args[0];
        final int widthFactor = args.length >= 2 ? Integer.parseInt(args[1]) : 50;
        final TSPTWProblem problem = readInstance(file);

        final TSPTWRelax relax = new TSPTWRelax(problem);
        final TSPTWRanking ranking = new TSPTWRanking();

        final TSPTWWidth width = new TSPTWWidth(problem.nbVars(), widthFactor);
        final VariableHeuristic<TSPTWState> varh = new DefaultVariableHeuristic<>();
        final Frontier<TSPTWState> frontier = new SimpleFrontier<>(ranking);


        ParallelSolver<TSPTWState> solver = new ParallelSolver<>(
                Runtime.getRuntime().availableProcessors(),
                problem,
                relax,
                varh,
                ranking,
                width,
                frontier
        );

        long start = System.currentTimeMillis();
        SearchStatistics stat = solver.maximize();
        double duration = (System.currentTimeMillis() - start) / 1000.0;


        String solutionStr;
        if (solver.bestSolution().isPresent()) {
            int[] solution = solver.bestSolution().map(decisions -> {
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
