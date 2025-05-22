package org.ddolib.ddo.examples.tsp;

import org.ddolib.ddo.core.*;
import org.ddolib.ddo.implem.frontier.SimpleFrontier;
import org.ddolib.ddo.implem.heuristics.DefaultVariableHeuristic;
import org.ddolib.ddo.implem.heuristics.FixedWidth;
import org.ddolib.ddo.implem.solver.ParallelSolver;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public final class TSPMain {

    public static TSPPRoblem genInstance(int n) {

        int[] x = new int[n];
        int[] y = new int[n];
        Random r = new Random(1);
        for(int i = 0 ; i < n ;  i++){
            x[i] = r.nextInt(100);
            y[i] = r.nextInt(100);
        }

        int[][] distance = new int[n][];
        for(int i = 0 ; i < n ;  i++){
            distance[i] = new int[n];
            for(int j = 0 ; j < n ;  j++){
                distance[i][j] = dist(x[i] - x[j] , y[i]-y[j]);
            }
        }
        return new TSPPRoblem(distance);
    }


    /**
     * Creates instance from data files.<br>
     * <p>
     * The expected format is the following:
     * <ul>
     *     <li>
     *         The first line must contain the number of points
     *     </li>
     *     <li>
     *         The distance matrix.
     *     </li>
     * </ul>
     *
     * @param fileName The path to the input file.
     * @throws IOException If something goes wrong while reading input file.
     */
    public static TSPPRoblem loadInstance(String fileName) throws IOException {
        int numVar = 0;
        int[][] myDistanceMatrix = null;
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
                    myDistanceMatrix = new int[numVar][numVar];
                } else if (1 <= lineCount && lineCount <= numVar) {
                    int i = lineCount - 1;
                    String[] distanceFromI = line.split("\\s+");
                    myDistanceMatrix[i] = Arrays.stream(distanceFromI).mapToInt(Integer::parseInt).toArray();
                }
                lineCount++;
            }
        }
        return new TSPPRoblem(myDistanceMatrix);
    }

    static int dist(int dx, int dy){
        return (int)Math.sqrt(dx*dx+dy*dy);
    }

    public static void main(final String[] args) {

        final TSPPRoblem problem = genInstance(15);

        System.out.println("problem:" + problem);
        System.out.println("initState:" + problem.initialState());
        solveTsp(problem);
        System.out.println("end");
    }

    public static void solveTsp(TSPPRoblem problem){

        final TSPRelax                    relax = new TSPRelax(problem);
        final TSPRanking                ranking = new TSPRanking();
        final FixedWidth<TSPState> width = new FixedWidth<>(500);
        final DefaultVariableHeuristic varh = new DefaultVariableHeuristic();

        final Frontier<TSPState> frontier = new SimpleFrontier<>(ranking);
        final Solver solver = new ParallelSolver<>(
                Runtime.getRuntime().availableProcessors()/2,
                problem,
                relax,
                varh,
                ranking,
                width,
                frontier);

        SearchStatistics stats = solver.maximize(2);

        int[] solution = solver.bestSolution()
                .map(decisions -> {
                    int[] route = new int[problem.nbVars()+1];
                    route[0] = 0;
                    for (Decision d : decisions) {
                        route[d.var()+1] = d.val();
                    }
                    return route;
                })
                .get();

        System.out.println(stats);
        System.out.printf("Objective: %d%n", solver.bestValue().get());
        System.out.println("eval from scratch: " + problem.eval(solution));
        System.out.printf("Solution : %s%n", Arrays.toString(solution));
    }
}



