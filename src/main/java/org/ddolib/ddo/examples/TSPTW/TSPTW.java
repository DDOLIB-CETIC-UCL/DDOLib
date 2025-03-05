package org.ddolib.ddo.examples.TSPTW;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Frontier;
import org.ddolib.ddo.core.SearchStatistics;
import org.ddolib.ddo.heuristics.VariableHeuristic;
import org.ddolib.ddo.implem.frontier.SimpleFrontier;
import org.ddolib.ddo.implem.heuristics.DefaultVariableHeuristic;
import org.ddolib.ddo.implem.heuristics.FixedWidth;
import org.ddolib.ddo.implem.solver.ParallelSolver;
import org.ddolib.ddo.implem.solver.SequentialSolver;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

public class TSPTW {


    public static TSPTWProblem readInstance(String fileName) throws IOException {
        int numVar = 0;
        int[][] distance = new int[0][0];
        TimeWindow[] timeWindows = new TimeWindow[0];
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            int lineCount = 0;

            String line;

            while ((line = br.readLine()) != null) {
                //Skip comment
                if (line.startsWith("#") || line.isEmpty()) {
                    continue;
                }

                if (lineCount == 0) {
                    numVar = Integer.parseInt(line);
                    distance = new int[numVar][numVar];
                    timeWindows = new TimeWindow[numVar];
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
            return new TSPTWProblem(distance, timeWindows);
        }
    }


    public static void main(String[] args) throws IOException {
        TSPTWProblem problem = readInstance("data/TSPTW/AFG/rbg031a.tw");

        TSPTWRelax relax = new TSPTWRelax(problem);
        TSPTWRanking ranking = new TSPTWRanking();

        final TSPTWWidth width = new TSPTWWidth(problem.nbVars(), 20);
        final VariableHeuristic<TSPTWState> varh = new DefaultVariableHeuristic<>();
        final Frontier<TSPTWState> frontier = new SimpleFrontier<>(ranking);


        ParallelSolver<TSPTWState> solver = new ParallelSolver<>(Runtime.getRuntime().availableProcessors(), problem, relax, varh, ranking, width,
                frontier);
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


        System.out.printf("Duration : %.3f seconds%n", duration);
        System.out.printf("Objective: %s%n", bestStr);
        System.out.printf("Solution : %s%n", solutionStr);
        System.out.println(stat);


    }
}
